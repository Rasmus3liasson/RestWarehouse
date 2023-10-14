package org.laboration3.resource.Exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestException extends ExceptionMapper<jakarta.ws.rs.BadRequestException> {

    @Override
    protected String resMessage(jakarta.ws.rs.BadRequestException exception) {
        return exception.getMessage();
    }

    @Override
    protected Response.Status resStatus() {
        return Response.Status.BAD_REQUEST;

    }
}
