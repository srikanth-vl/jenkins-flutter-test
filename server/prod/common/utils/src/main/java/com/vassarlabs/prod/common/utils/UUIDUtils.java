package com.vassarlabs.prod.common.utils;

import java.util.UUID;

import com.fasterxml.uuid.Generators;

public class UUIDUtils {

	private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

	public static long getTimeFromUUID(UUID uuid) {
		return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
	}
	
	public static UUID getTrueUUID() {
		return Generators.randomBasedGenerator().generate();
	}
	
	public static String getUUID() {
		return getTrueUUID().toString();
	}

	public static UUID getTrueTimeUUID() {
		return Generators.timeBasedGenerator().generate();
	}
	
	public static String getTimeUUID() {
		return getTrueTimeUUID().toString();
	}
	
	public static UUID toUUID(String inUUID) {
		if (StringUtils.isNullOrEmpty(inUUID)) {
			return null;
		}
		return UUID.fromString(inUUID);
	}
	
	public static UUID getDefaultUUID() {
		return new UUID(0L, 0L);
	}
	
	public static UUID generateUUIDFromString(String string) {
		return UUID.nameUUIDFromBytes(string.getBytes());
	}
}
