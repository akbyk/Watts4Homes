package com.akbyk.watts4homes.core.event;

//Consumed after commit
public record HomeRegisteredInternalEvent(HomeRegisteredEvent payload) {}