package org.laboration3.resource.customException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public abstract class ExceptionMapper<E extends Throwable> implements jakarta.ws.rs.ext.ExceptionMapper<E> {
    @Override
    public Response toResponse(E exception) {
        String errorMessage = resMessage(exception);
        return Response.status(resStatus())
                .entity("{\"error\": \"" + errorMessage + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
    protected abstract String resMessage(E exception);
    protected abstract Response.Status resStatus();
}
