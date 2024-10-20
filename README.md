# LIBROS Y MÁS

## Introducción

Este proyecto implementa un sistema de gestión de inventarios para la librería "Libros y Más", utilizando un **Árbol B** para organizar los datos de los libros. El sistema maneja operaciones de inserción, modificación, eliminación y búsqueda de libros, cuyas salidas se encriptan utilizando el algoritmo **DES (Data Encryption Standard)** para garantizar la seguridad de la información.

El objetivo del sistema es proporcionar una solución eficiente y segura para la manipulación de los datos del inventario, asegurando la confidencialidad de los datos intercambiados entre las sucursales de la librería.

## Algoritmo de Encriptación: DES

El algoritmo de encriptación utilizado es **DES (Data Encryption Standard)** en **Modo ECB**. Este algoritmo permite encriptar los datos del inventario, garantizando que los resultados de las búsquedas sean seguros en caso de interceptación. El modo de operación ECB encripta bloques de 64 bits de forma independiente, lo que simplifica la implementación.

### Detalles de la encriptación:
- **Clave utilizada**: `"ok:uo1IN"`
- **Tamaño del bloque**: 64 bits (8 bytes)
- **Padding**: PKCS#7 (para completar bloques)
- **Modo de operación**: ECB (Electronic Codebook)

## Estructura de Datos Utilizada: Árbol B

El sistema de inventarios utiliza un **Árbol B** para almacenar y gestionar los datos de los libros. El Árbol B es una estructura de datos balanceada que permite realizar operaciones de búsqueda, inserción, modificación y eliminación de manera eficiente.

### Operaciones principales sobre el Árbol B:
- **insert(JSONObject book)**: Inserta un nuevo libro en el árbol, manteniendo los libros ordenados por ISBN.
- **updateBook(String isbn, Map<String, Object> updateData)**: Modifica la información de un libro identificado por su ISBN.
- **removeBook(String isbn)**: Elimina un libro del Árbol B, ajustando los nodos si es necesario.
- **searchByName(String name)**: Busca un libro en el Árbol B por su nombre.

La elección del **Árbol B** se debe a su capacidad de mantener el inventario balanceado, permitiendo un acceso rápido tanto para inserciones como para búsquedas. Esto es crucial en sistemas donde se manejan grandes cantidades de libros, como en esta librería.

## Procesamiento de Archivos CSV

El sistema procesa dos archivos CSV principales:
1. **Archivo de operaciones** (`lab01_books.csv`): Contiene las operaciones de inserción, modificación y eliminación de libros.
2. **Archivo de búsqueda** (`lab01_search.csv`): Contiene las consultas para buscar libros en el inventario.

El sistema genera un archivo de salida `output.txt` con los resultados de las búsquedas, que luego es encriptado usando DES.

## Encriptación y Desencriptación

Los resultados de las búsquedas se encriptan y almacenan en un archivo `.enc` para garantizar la seguridad de los datos. El programa permite tanto la **encriptación** como la **desencriptación** de los archivos utilizando la misma clave y modo de operación.

### Métodos principales:
- **encrypt(String data, String key)**: Encripta los datos de salida utilizando DES.
- **decrypt(byte[] encryptedData, String key)**: Desencripta los datos previamente encriptados.
- **saveEncryptedFile(byte[] encryptedData, String filePath)**: Guarda los datos encriptados en un archivo binario.
- **readEncryptedFile(String filePath)**: Lee un archivo encriptado y devuelve su contenido.

## Instrucciones para Ejecutar el Proyecto

### Requisitos:
- **Java Development Kit (JDK)** 1.8 o superior.
- Biblioteca estándar de Java (`javax.crypto`).

### Pasos para ejecutar:

1. **Procesar el archivo CSV de operaciones**:
   - Ejecuta el método `ReaderCSV()` para procesar el archivo `lab01_books.csv` e insertar, modificar o eliminar libros en el Árbol B.

2. **Procesar el archivo de búsqueda**:
   - Ejecuta el método `Exit()` para procesar el archivo `lab01_search.csv` y generar el archivo `output.txt` con los resultados de las búsquedas.

3. **Encriptar el archivo de salida**:
   - Utiliza el método `encrypt()` para encriptar el archivo `output.txt` y guardarlo como `bitacora_encriptada.enc`.

4. **Desencriptar el archivo**:
   - Utiliza el método `decrypt()` para desencriptar el archivo encriptado y obtener los resultados originales.

### Ejecución:
```bash
java Main
```
---

### bitácora de entrada:
```bash
INSERT;{"isbn": "9780856397349", "name": "Then go hope attention friend peace create each.", "author": "Eric Fleming", "category": "Biography", "price": "15.78", "quantity": "690"}
SEARCH;{"name": "Then go hope attention friend peace create each."}
```
---

###  bitácora de salida:
```bash
{"isbn":"9780856397349","name":"Then go hope attention friend peace create each.","author":"Eric Fleming","category":"Biography","price":"74.60","quantity":"690"}
```
---

### Decisiones:
1. **Árbol B**:
   - Se seleccionó un Árbol B debido a su eficiencia para manejar grandes volúmenes de datos y realizar operaciones de búsqueda, inserción y eliminación de manera rápida y eficiente.

2. **DES para Encriptación**:
   -  Se utilizó DES para encriptar los datos sensibles en el archivo de salida, protegiendo así la información durante la transmisión entre sucursales. Aunque DES no es el algoritmo más moderno, es adecuado para este proyecto por su simplicidad y facilidad de implementación.
3. **Modo ECB con Padding PKCS#7**:
   - El modo ECB fue elegido por su simplicidad y el padding PKCS#7 asegura que los datos se ajusten a los bloques de 64 bits requeridos por DES.

### Conclusión:
Este proyecto proporciona una solución segura y eficiente para la gestión de inventarios de la librería "Libros y Más", utilizando un Árbol B para las operaciones de búsqueda y un esquema de encriptación DES para proteger los resultados.


