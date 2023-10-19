FROM bitnami/wildfly:29.0.1
LABEL authors="rasmuseliasson"

COPY /target/RestWarehouse-1.0-SNAPSHOT.war /app
