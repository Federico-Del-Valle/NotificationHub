Notification Hub

Sistema de mensajería unificada que permite enviar notificaciones a múltiples plataformas (Slack, Telegram) mediante una única API.  
Proyecto desarrollado con **Spring Boot 3**, **JPA**, **JWT** y documentado con **Swagger**.

## Tecnologias utilizadas

- Java 21  
- Spring Boot 3.5  
- Spring Data JPA  
- Spring Security (JWT)  
- Spring Validation  
- Swagger UI 
- Lombok  
- Base de datos : MySql

---

## Configuracion de perfiles

El proyecto tiene dos perfiles de ejecución configurados:

Perfiles: 
**dev** 
**prod** 
---
## Cómo ejecutar el proyecto
### ** Opción 1 **
No requiere instalar ninguna base de datos.
Levantar la aplicacion
mvn spring-boot:run
Levantara en [http://localhost:8080/swagger-ui.html]

###  **Opción 3 **  
Está pensado para el entorno del desarrollador o para cuando quieras mostrar el proyecto con datos reales y persistentes.
- Tener **MySQL** instalado y corriendo.
- Crear una base de datos vacía llamada **noticationhub** :
  ###CREATE DATABASE noticationhub;###
Levantar la aplicacion
  mvn spring-boot:run -Dspring.profiles.active=prod
