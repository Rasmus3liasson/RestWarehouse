package org.laboration3.resource.custonException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundException extends ExceptionMapper <jakarta.ws.rs.NotFoundException> {
    @Override
    protected String resMessage(jakarta.ws.rs.NotFoundException exception) {
        return exception.getMessage();
    }

    @Override
    protected Response.Status resStatus() {
        return Response.Status.NOT_FOUND;
    }

}