package com.iheartsimplelife;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

public class TerritoryItemProcessor implements ItemProcessor<TerritoryAddress, TerritoryAddress> {
    private static final Logger log = LoggerFactory.getLogger(TerritoryItemProcessor.class);

    @Override
    public TerritoryAddress process(final TerritoryAddress territoryAddress) throws Exception {
        final String fullName = territoryAddress.getFullName();
        final String street = territoryAddress.getStreet();
        final String city = territoryAddress.getCity();
        final String state = territoryAddress.getState();
        final String zip = territoryAddress.getZipCode();
        final String notes = territoryAddress.getNotes();

        String[] fullNameArr = fullName.split(" ");
        String firstName = "";
        String lastName = "";
        int fullNameArrLength = fullNameArr.length;
        int stateId = 0;

        if (fullNameArrLength > 0) {
            firstName = fullNameArr[0];

            if (fullNameArrLength == 2) {
                lastName = fullNameArr[1];
            } else if (fullNameArr.length > 2) {
                for (int i = 1; i < fullNameArr.length; i++) {
                    if (i > 1) {
                        lastName += " ";
                    }

                    lastName += fullNameArr[i];
                }
            }
        }

        if (state.equalsIgnoreCase("NY")) {
            stateId = 1;
        } else if (state.equalsIgnoreCase("CT")) {
            stateId = 2;
        }

        // TODO: Set all the other default fields here

        final TerritoryAddress transformedTerritoryAddress = new TerritoryAddress(firstName, lastName, street, city, stateId, zip, notes);

        log.info("Converting (" + territoryAddress + ") into (" + transformedTerritoryAddress + ")");

        return transformedTerritoryAddress;
    }
}
