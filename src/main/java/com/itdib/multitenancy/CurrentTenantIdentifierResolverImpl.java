package com.itdib.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    private static final String DEFAULT_TENANT_ID = "test1";

    @Override
    public String resolveCurrentTenantIdentifier() {
    			String tenant = TenantContextHolder.getTenant();
                return tenant!=null && !tenant.equalsIgnoreCase("") ? tenant : DEFAULT_TENANT_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}
