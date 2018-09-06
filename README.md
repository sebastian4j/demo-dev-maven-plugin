# demo-dev-maven-plugin

Plugin **maven** para monitorear la carpeta del código fuente permitiendo compilarlo y ejecutar el **jar** resultante cuando detecta algún cambio.

El objetivo es permitir utilizar un IDE y no tener que volver a compilar el código y luego lanzar la aplicación.

Las operaciones que se ejecutan son:
- mvn package
- java -jar {project.build.director}/{project.build.finalName}.{project.packaging}
 ejemplo: java -jar target/a.jar

Para poder utilizando se ejecuta lo siguiente:

> mvn com.sebastian.plugins:demo-dev-maven-plugin:1.3-SNAPSHOT:desa

Actualmente lo que hace es utilizar **mvn package** y luego lanzar el ejecutable **Java** sin realizar alguna optimización, tiene que estar disponible en el path *mvn* y *java*

#### uso:

En el **pom.xml** proyecto que estemos desarrollando hay que incluir lo siguiente:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.sebastian.plugins</groupId>
			<artifactId>demo-dev-maven-plugin</artifactId>
			<version>[1,)</version>
		</plugin>
		...
	</plugins>
</build>
```

luego se ejecuta en el proyecto con la siguiente instrucción maven:
> mvn com.sebastian.plugins:demo-dev-maven-plugin:1.3-SNAPSHOT:desa

Se puede ejecutar con eclipse y se mantendra escuchando cambios en el arbol de directorios y se puede detener desde el mismo IDE.

> El repositorio https://github.com/sebastian4j/demo-kum es un ejemplo de como utilizar el plugin

___
La intención es poder implementar una solución que permita realizar en forma eficiente la labor de actualizar la aplicación para reflejar los cambios, esta versión por lo menos... funciona 😬  

 

