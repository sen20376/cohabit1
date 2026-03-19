package eu.qerkinaj.cohabit.catalog.client.currency.producer;

import eu.qerkinaj.cohabit.catalog.client.currency.SupportedCurrency;
import eu.qerkinaj.cohabit.catalog.client.currency.qualifier.CurrentCurrency;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;

@RequestScoped
public class CurrencyProducer {

    private static final Logger LOG = Logger.getLogger(CurrencyProducer.class);

    @Context
    UriInfo uriInfo;


    @Produces
    @CurrentCurrency
    public SupportedCurrency getCurrency() {
        String currencyParam = uriInfo.getQueryParameters().getFirst("currency");

        LOG.tracef("entering getCurrency(), raw param='%s'", currencyParam);

        if (currencyParam != null && !currencyParam.isBlank()) {
            String candidate = currencyParam.trim().toUpperCase();

            LOG.debugf("normalized currency param = %s", candidate);

            try {
                SupportedCurrency sc = SupportedCurrency.valueOf(candidate);

                LOG.infof("using client-passed currency: %s", sc);
                LOG.tracef("exiting getCurrency() -> %s", sc);

                return sc;
            } catch (IllegalArgumentException iae) {
                LOG.warnf(iae, "invalid currency '%s', defaulting to EUR", candidate);
            }
        } else {
            LOG.debug("no 'currency' query param provided");
        }

        SupportedCurrency fallback = SupportedCurrency.EUR;
        LOG.tracef("exiting getCurrency() -> %s", fallback);
        return fallback;
    }
}