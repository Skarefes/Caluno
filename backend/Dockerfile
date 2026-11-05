
# ETAPA 1 - Compilação
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o Maven e as dependências
COPY pom.xml .

# Copia o código-fonte
COPY src ./src

# Compila o projeto
RUN mvn clean package -DskipTests

# ETAPA 2 - Execução
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o JAR gerado
COPY --from=build /app/target/*.jar app.jar

# Porta da aplicação
EXPOSE 8080

# Inicia o Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
