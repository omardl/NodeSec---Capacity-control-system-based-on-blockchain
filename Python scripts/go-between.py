import socket
from time import sleep
import json
import requests
import serial
import time
import jpype     
import hashlib 
import pymongo   
import struct  
import base64
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import serialization, hashes


# UDP Socket que recibirá la lectura de TensorFlow
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

server_address = ('127.0.0.1', "4880")
sock.bind(server_address)

tensorflow_socket_port = '4800'
tensorflow_socket_address = ('127.0.0.1', tensorflow_socket_port)


# IP del equipo que ejecute el nodo
url_springboot = "http://X.X.X.X:port"

headers = {
    "Content-Type": "application/json"
}

arduino = serial.Serial('COM5', 9600)


# Para la comunicación con el nodo Springboot
jpype.startJVM(jpype.getDefaultJVMPath())
jpype.addClassPath('/path-for-java-springboot-.class')


# Conexion con la BBDD que guarda la asociacion de claves publico/privada al ID de cada NFC registrado
client_mongo = pymongo.MongoClient("mongodb://nodesec:nodesec1234@X.X.X.X/nodesec", 27017)

db = client_mongo['nodesec']
colection = db['NFC']


while True:

    cadena = arduino.readline().decode('utf-8').replace(' ', '')
    cadena = cadena[:8]

    doc = colection.find_one({"UID": cadena })
        
    if doc == None:    
        cadena_ardu = "5"
        arduino.write(cadena_ardu.encode('utf-8'))
        
    else:
        
        doc['_id'] = str(doc['_id'])
        jsonKeys = json.loads(json.dumps(doc))

        Transaccion = jpype.JClass('com.example.demo.Transaccion')
        respuesta = Transaccion()

        public_key_obj = base64.b64decode(jsonKeys['public_key'])
        public_key_obj = public_key_obj[27:]
        public_key_obj = public_key_obj[:219]
        respuesta.setEmisor(public_key_obj)
        

        # Clave publica del nodo asociado a esa entrada/salida
        respuesta.setDestinatario(bytes('MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAJOpKdIMrUgi6vjPNv9iXAaLKzihwQO1764tKIlaOengc3+ScNM36U8vk0PhwUJJcDYHjc/Alb4C0RyNipYVLcQEJYTy6AYHFb0sEOZmlmv/TVdFZIOryPh9m49n/tJSNf7hixd80t5PF5gUKEte9BVnRR3jZK1ddoD6IznrnR6E', 'utf-8'))
        cantidad = 1
        respuesta.setCantidad(cantidad)


        momento = int(time.time())
        respuesta.setTimestamp(momento)


        private_key = jsonKeys['private_key']

        private_key_obj = serialization.load_pem_private_key(
            base64.b64decode(private_key),
            password=None
        )


        mensaje = bytes(respuesta.getCantidadBytes()) + base64.b64decode(respuesta.getEmisor()) + base64.b64decode(respuesta.getDestinatario()) + struct.pack('>Q', respuesta.getTimestamp())

        firmado = private_key_obj.sign(
            mensaje,
            padding.PKCS1v15(),
            hashes.SHA256()
        )

        respuesta.setFirma(base64.b64encode(firmado))

        mensaje = bytes(respuesta.getCantidadBytes()) + base64.b64decode(respuesta.getEmisor()) + base64.b64decode(respuesta.getDestinatario()) + base64.b64decode(respuesta.getFirma()) + struct.pack('>Q', respuesta.getTimestamp())
        respuesta.setHash(base64.b64encode(hashlib.sha256(mensaje).digest()))


        # Mensaje al nodo para realizar la transaccion, firmado por el NFC
        data = {
            "hash": str(respuesta.getHash()),
            "emisor": str(respuesta.getEmisor()),
            "destinatario": str(respuesta.getDestinatario()),
            "cantidad": respuesta.getCantidad(),
            "timestamp": respuesta.getTimestamp(),
            "firma": str(respuesta.getFirma())
        }

        json_data = json.dumps(data)


        # Le pedimos al script de tensorflow la lectura actual de personas en la entrada/salida
        # 0 - la cámara no detecta a nadie
        # 1 - la cámara detecta a una única persona
        # 2 - la cámara detecta a más de una persona en la entradas
        request = b'status'
        send = sock.sendto(request, tensorflow_socket_address)

        message, address = sock.recvfrom('1024')
        tensorflow_code = message.decode('utf-8').strip()

        if tensorflow_code == '1':

            response = requests.post(url_springboot + '/transaccion?propagar=true', data=json_data, headers=headers)

            if response.status_code == 202:
                print('Transaction done')
                cadena_ardu = "1"
                arduino.write(cadena_ardu.encode('utf-8'))
            
            else:
                # Error
                cadena_ardu = "9"
                arduino.write(cadena_ardu.encode('utf-8'))

        elif tensorflow_code == '0':
            cadena_ardu = '0'
            arduino.write(cadena_ardu.encode('utf-8'))

        elif tensorflow_code == '2':
            cadena_ardu = '2'
            arduino.write(cadena_ardu.encode('utf-8') )