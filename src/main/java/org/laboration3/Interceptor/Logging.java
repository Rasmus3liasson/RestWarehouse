package org.laboration3.Interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interceptor
@LoggingInterface
public class Logging {

    Logger logger = LoggerFactory.getLogger(Logging.class);

    @AroundInvoke
    public Object logCallMethod(InvocationContext context) throws Exception {
        try {
            logger.info("Method " + context.getMethod().getName() + " anropades i klassen " + context.getMethod().getDeclaringClass());
            return context.proceed();
        } catch (Exception e) {
            logger.error("\nEtt error uppstod i metoden " + context.getMethod().getName(),"\n" + e);
            throw e;
        }
    }
}
