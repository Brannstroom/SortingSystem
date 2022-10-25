package me.brannstrom.sortingsystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class StringHandler {

	public static String plainText(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

}
