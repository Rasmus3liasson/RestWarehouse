FROM quay.io/wildfly/wildfly:latest

# FROM bitnami/wildfly:latest
LABEL authors="rasmuseliasson"

WORKDIR /app

COPY /target/RestWarehouse-1.0-SNAPSHOT.war /app