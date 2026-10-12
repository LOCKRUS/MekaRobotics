package com.io.mekarobotics.network.container;

public interface ISyncableData {

    boolean isDirty();

    PropertyData getPropertyData(short property);
}
