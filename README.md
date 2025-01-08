Tecmilenio Social
Tecmilenio Social es una aplicación móvil desarrollada en Kotlin que simula una red social diseñada exclusivamente para la comunidad de Tecmilenio. La aplicación ofrece funciones esenciales para interactuar, compartir contenido y conectar con otros usuarios dentro de la comunidad académica.

Características
Inicio de sesión seguro
Los usuarios pueden iniciar sesión mediante credenciales únicas para acceder a su perfil.

Registro de nuevos usuarios
La plataforma permite a nuevos miembros registrarse fácilmente.

Gestión de amigos

Agregar amigos: Conecta con otros usuarios.
Quitar amigos: Administra tus conexiones según tus preferencias.
Interacción con publicaciones

Ver publicaciones: Explora el contenido compartido por otros usuarios.
Publicar contenido: Comparte tus pensamientos, logros y experiencias.
Tecnologías Utilizadas
Lenguaje de programación: Kotlin
APIs: Integración de servicios RESTful para manejar el backend.
Framework: Android SDK
Gestión de datos: Retrofit para la comunicación con las APIs.
Base de datos local: Room para almacenamiento local de datos.
Autenticación: Tokens de autenticación JWT para seguridad.
Requisitos Previos
Antes de ejecutar la aplicación, asegúrate de tener:

Android Studio instalado.
Una clave API configurada en un archivo secrets.properties para las integraciones.
Emulador o dispositivo físico para pruebas.
Cómo Configurar el Proyecto
Clona el repositorio:

bash
Copiar código
git clone https://github.com/tu-usuario/tecmilenio-social.git  
Abre el proyecto en Android Studio:
Navega a la carpeta del proyecto y ábrela en Android Studio.

Configura las variables de entorno:

Crea un archivo secrets.properties en la raíz del proyecto.
Agrega tus claves API:
makefile
Copiar código
API_BASE_URL=https://api.tu-backend.com
API_KEY=tu-clave-api
Instala dependencias:
Android Studio descargará automáticamente las dependencias declaradas en el archivo build.gradle.

Ejecuta la aplicación:

Conecta un dispositivo físico o configura un emulador.
Haz clic en el botón Run para compilar y ejecutar la aplicación.
Estructura del Proyecto
UI: Actividades y fragmentos para las interfaces de usuario.
Data: Manejo de modelos y repositorios, junto con la integración de APIs.
Network: Configuración de Retrofit y controladores de API.
Local: Configuración de Room para almacenamiento local.
Contribuciones
¡Tu participación es bienvenida! Si deseas contribuir, sigue estos pasos:

Haz un fork del repositorio.
Crea una rama para tu funcionalidad:
bash
Copiar código
git checkout -b feature/nueva-funcionalidad  
Realiza los cambios necesarios y haz commit:
bash
Copiar código
git commit -m "Añadí una nueva funcionalidad"  
Haz push a la rama:
bash
Copiar código
git push origin feature/nueva-funcionalidad  
Abre un Pull Request en GitHub.
Licencia
Este proyecto está bajo la licencia MIT, lo que permite su uso y modificación.
