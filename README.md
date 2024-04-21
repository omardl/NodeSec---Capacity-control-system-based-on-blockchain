# NodeSec - Sistema de control de aforos basado en blockchain

## Objetivos

Diseño y configuración del prototipo de un sistema de control de aforos que alcance los siguientes objetivos:

- Control y monitorización del aforo de manera fiable y pública, asegurando un correcto cumplimiento de las normativas y registrando la información para que sea accesible en cualquier momento a posteriori. 

- Conteo de personas que tenga en cuenta la distribución interna del aforo, pudiendo dividir el recinto en zonas y llevando el registro de entrada y salida tanto de las zonas individuales como del recinto general.

- Automatización de las entradas y salidas permitiendo el acceso de manera fluida y, a su vez, evitando accesos no permitidos.

- Monitorización y vigilancia de cada punto de entrada y salida y trazabilidad individual del recorrido de cada asistente.

- Escalabilidad del sistema que permita añadir nuevas funcionalidades en el futuro.

## NodeSec

El prototipo creado contiende las siguientes características:

- La sistema está basado en el funcionamiento de una red blockchain privada, permitiendo el almacenamiento de la información de manera fiable, segura y permitiendo su consulta en cualquier momento en tiempo real.

- Cada punto de entrada y salida será un nodo de la blockchain, permitiendo la división del recinto principal en diferentes zonas, y el acceso y salida se realizará mediante NFCs de manera automatizada.

- En cada nodo, además, se dispondrá de una cámara web para la vigilancia que, mediante Tensorflow, usará una red neuronal preentrenada para el conteo de personas automático, permitiendo y asegurando la entrada de una sóla persona a la vez. 

- El equipo de cada nodo consistirá, a mayores de la cámara, en una Raspberry y un arduino al que se añadirán el lector de NFCs y una pantalla led que emita una serie de mensajes a los asistentes. Dichos nodos serán denominados "Nodos de acceso".

- Este sistema permite la escalabilidad, pudiendo incluir mejoras de manera rápida y sencilla como, por ejemplo, añadir sensores térmicos en determinados nodo para medir las condiciones de temperatura de los asistentes en caso de nuevos brotes epidémicos o la incorporación de nuevos modelos de redes neuronales que permitan la detección de objetos peligrosos, emitiendo el aviso correspondiente al personal de seguridad e impidiendo la entrada al recinto.

- A mayores de los "Nodos de acceso", se podrá monitorizar el sistema ejecutando un nuevo nodo en un ordenador. Dichos nodos serán denominados "Nodos web" y, en lugar de funcionar como puntos de acceso, funcionarán como puntos de monitorización, permitiendo acceder a la información de la blockchain mediante una interfaz web en Node-RED. A través de dicha interfaz podrá consultarse el aforo actual de cada zona, su registro de entradas y salidas y el histórico de los mismos.

- ## Red blockchain

- La red es una API REST escrita en Java mediante el framework SpringBoot.

- Su funcionamiento es similar a la red blockchain de Bitcoin, pero más sencillo: una red P2P que utiliza el algoritmo de prueba de trabajo.

- Las entradas y salidas de cada nodo se representan mediante una transacción. Los ID de los NFC usados por el sistema deberán estar registrados, almacenados en una base de datos interna y asociados a un par de claves pública y privada; siendo accesibles por cada nodo. Cada transacción estará compuesta por: NFC emisor, nodo receptor, sello temporal, firma y un hash de todo el contenido del mensaje.

- Las transacciones, además, serán transmitidas al resto de nodos y validadas, generando bloques compuestos por un número determinado (y configurable) de transacciones que, a su vez, serán validados por los demás nodos antes de incluirse en la red.

- Los nodos se comunicarán entre ellos mediante peticiones GET/POST/DELETE para añadir transacciones, obtener información de los nodos vecinos al incorporarse un nuevo nodo a la red, añadir o eliminar bloques, añadir o eliminar nuevos nodos en la red, etc.

- La aplicación SpringBoot se ejecuta en cada nodo y podrán ser configurados ciertos parámetros como el número de transacciones por bloque, la dificultad de minado, la dirección del nodo máster (el nodo que se ejecuta en primer lugar inicializando la red), etc; a través de un archivo de configuración XML.

- 
## Nodo de acceso

![Nodo acceso](https://github.com/omardl/NodeSec---Capacity-control-system-based-on-blockchain/assets/105445540/e7fba276-c165-484c-8120-a425c8953843)

### Componentes conectados al arduino:

- Lector de NFCs.

- Pantalla LCD que mostrará diferentes avisos al usuario (mensaje de bienvenida, error de lectura del NFC, etc).

- LEDs y buzzer para la notificación de avisos (entrada aceptada y diferentes errores).


### TensorFlow para el conteo de asistentes:

- Se ha usado un modelo preenteenado que detecta la cantidad de personas observadas por la cámara. Específicamente, uno de los modelos disponibles en TensorFlow Lite, la versión adaptada a dispositivos móviles y Raspberry.

- El código de dicho modelo ha sido modificado. Esperará un mensaje UDP en un puerto determinado y responderá con la cantidad de personas detectadas por la cámara en dicho momento. El intercambio de mensajes a través de UDP se realizará con el script mediador escrito en python, mencionado más adelante.

- Este comportamiento permitirá, sincronizado con la lectura del NFC, que sólo una persona pueda acceder a la vez.

- En caso de no detectar a nadie (el asistente se ha alejado de la entrada) o detectar a más de una persona, un mensaje específico será mostrado en la pantalla LCD conectada al arduino indicando al usuario que se acerque a la entrada o que no intente colarse.


### Mediador

- Se ha programado un script en python que ocupará el rol de intermediario entre el nodo de esa Raspberry, el arduino, TensorFlow y la base de datos MongoDB que contiene el par de claves asociado al ID del NFC.

- Recibirá la lectura del arduino cada vez que un NFC sea detectado por el lector, consultando en la base de datos si ese nodo está registrado para su uso como entrada al recinto. En caso negativo, mandará una respuesta al arduino para que muestre un mensaje en la pantalla LCD indicando la invalidez del NFC.

- Si la lectura es correcta, se construye un Json con los datos de la transacción y se envía un paquete UDP al modelo de TensorFlow para que indique la cantidad de personas detectadas a la entrada. Si es diferente a uno, indica al arduino que muestre el aviso correspondiente en la pantalla. 

- Si no ha ocurrido ningún error, envía la transacción al nodo SpringBoot ejecutandose en esa Raspberry. En caso de un error en la transacción por fallos en la blockchain o un error desconocido, se enviará un mensaje especial a la pantalla que indique un error crítico (posible mal funcionamiento en algún punto del nodo) y muestre el número de teléfono del personal técnico de mantenimiento. En este caso el buzzer y los leds parpadearán de manera permanente.

### Cámara web

- Paralelamente, la cámara estará transmitiendo un streaming mediante un servidor RSTP que permitirá monitorizar el punto de entrada/salida desde un nodo web.


## Nodo web

![Nodo web](https://github.com/omardl/NodeSec---Capacity-control-system-based-on-blockchain/assets/105445540/7189f581-e9fc-4d17-a7d4-7dce39f5cb14)

- El nodo web estará representado comunmente por un PC que funcionará como un nodo más de la blockchain pero no realizará ninguna transaccion en la misma. Dispondrá de una interfaz web creada con Node-RED que permitirá ver el aforo actual de cada zona y un registro de las entradas y salidas de cada una. La información de las transacciones es, a su vez, almacenada en una base de datos MongoDB. También se podrá monitorizar el streaming de cualquiera de los nodos de acceso.

![Dashboard_NodeSec_1](https://github.com/omardl/NodeSec---Capacity-control-system-based-on-blockchain/assets/105445540/e5550e36-7a83-4f72-884c-409d8fd2b236)

![Dashboard_NodeSec_2](https://github.com/omardl/NodeSec---Capacity-control-system-based-on-blockchain/assets/105445540/99c00ff4-0d66-4b94-8312-c8e11d91d18c)


### Proyecto desarrollado para la asignatura "Laboratorio de Proyectos" del grado en Ingeniería de Tecnologías de Telecomunicación de la UVigo. Todos los proyectos de la asignatura fueron expuestos al resto de estudiantes de la facultad y, posteriormente, al público general en el museo Verbum de Vigo durante un evento llamado "LPRO Days".


### Autores
- Omar Delgado
- Jorge Estévez
