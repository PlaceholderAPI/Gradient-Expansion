package com.sxtanna.mc;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Version;
import me.clip.placeholderapi.expansion.VersionSpecific;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Returns a HEX value on a gradient between two colors based on percentage
 * <p>
 * %gradient_{#hex 1}_{#hex 2}_{percentage:0..100}%
 */
public final class GradientExpansion extends PlaceholderExpansion implements VersionSpecific
{

	private static final Pattern VERSION_PATTERN = Pattern.compile("v(?<major>\\d+)_(?<minor>\\d+)(_(?<patch>.+))?");


	@Override
	public String getIdentifier()
	{
		return "gradient";
	}

	@Override
	public String getAuthor()
	{
		return "Sxtanna";
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}


	@Override
	public boolean isCompatibleWith(final Version version)
	{
		final Matcher matcher = VERSION_PATTERN.matcher(version.getVersion());

		return matcher.find() && Integer.valueOf(matcher.group("minor")).compareTo(16) >= 0;
	}

	@Override
	public List<String> getPlaceholders()
	{
		return Collections.singletonList("%gradient_{#hex 1}_{#hex 2}_{percentage:0..100}%");
	}

	@Override
	public String onRequest(final OfflinePlayer p, final String params)
	{
		final String[] parts = params.split("_");
		if (parts.length != 3)
		{
			return null;
		}

		final String hexOne  = parts[0];
		final String hexTwo  = parts[1];
		final String percent = parts[2];

		if (hexOne.length() != 7 || hexTwo.length() != 7 || percent.length() > 3)
		{
			return null;
		}

		try
		{
			final double percentage = Integer.parseInt(percent) / 100.0;

			final int hexOneR = Integer.parseInt(hexOne.substring(1, 3), 16);
			final int hexOneG = Integer.parseInt(hexOne.substring(3, 5), 16);
			final int hexOneB = Integer.parseInt(hexOne.substring(5, 7), 16);

			final int hexTwoR = Integer.parseInt(hexTwo.substring(1, 3), 16);
			final int hexTwoG = Integer.parseInt(hexTwo.substring(3, 5), 16);
			final int hexTwoB = Integer.parseInt(hexTwo.substring(5, 7), 16);

			final int middleR = (int) (hexOneR + ((hexTwoR - hexOneR) * percentage));
			final int middleG = (int) (hexOneG + ((hexTwoG - hexOneG) * percentage));
			final int middleB = (int) (hexOneB + ((hexTwoB - hexOneB) * percentage));

			return String.format("%02X%02X%02X", middleR, middleG, middleB);
		}
		catch (final NumberFormatException ignored)
		{
			return null;
		}
	}

}
