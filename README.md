# demo-dev-maven-plugin

Plugin **maven** para monitorear la carpeta del código fuente permitiendo compilarlo y ejecutar el **jar** resultante cuando detecta algún cambio.

El objetivo es permitir utilizar un IDE y no tener que volver a compilar el código y luego lanzar la aplicación.

Las operaciones que se ejecutan son:
- mvn package
- java -jar {project.build.director}/{project.build.finalName}.{project.packaging}
 ejemplo: java -jar target/a.jar

Para poder utilizando se ejecuta lo siguiente:

> mvn com.sebastian.plugins:demo-dev-maven-plugin:1.3-SNAPSHOT:desa
