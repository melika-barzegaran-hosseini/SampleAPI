package com.example.SampleAPI.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable>
{
    @Override
    public Response toResponse(Throwable e)
    {
        return Utility.createResponse(e, Response.Status.NOT_FOUND);
    }
}