package com.sxtanna.mc;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Version;
import me.clip.placeholderapi.expansion.VersionSpecific;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>%gradient_percent_{#hex1}_{#hex2}_{percentage:0..100}%</b>
 * <p>Returns a hex value by percentage between two colors</p>
 *
 * <br>
 *
 * <b>%gradient_message_{#hex1}_{#hex2}_message with colors%</b>
 * <p>Returns the supplied message with each letter colorized by the gradient</p>
 *
 * <br>
 */
public final class GradientExpansion extends PlaceholderExpansion implements VersionSpecific
{

	private static final Pattern      VERSION_PATTERN = Pattern.compile("v(?<major>\\d+)_(?<minor>\\d+)(_(?<patch>.+))?");
	private static final List<String> PLACEHOLDERS    = Arrays.asList("%gradient_percent_{#hex1}_{#hex2}_{percentage:0..100}%",
																	  "%gradient_message_{#hex1}_{#hex2}_message with colors%");


	@NotNull
	@Override
	public String getIdentifier()
	{
		return "gradient";
	}

	@NotNull
	@Override
	public String getAuthor()
	{
		return "Sxtanna";
	}

	@NotNull
	@Override
	public String getVersion()
	{
		return "1.0";
	}


	@Override
	public boolean isCompatibleWith(@NotNull final Version version)
	{
		final Matcher matcher = VERSION_PATTERN.matcher(version.getVersion());

		return matcher.find() && Integer.valueOf(matcher.group("minor")).compareTo(16) >= 0;
	}

	@NotNull
	@Override
	public List<String> getPlaceholders()
	{
		return PLACEHOLDERS;
	}


	@Nullable
	@Override
	public String onRequest(@Nullable final OfflinePlayer player, @NotNull final String params)
	{
		try
		{
			final int marker = params.indexOf('_');

			String remaining = params.substring(marker + 1);

			final int    headMarker = remaining.indexOf('_');
			final String head       = remaining.substring(0, headMarker);

			remaining = remaining.substring(headMarker + 1);


			final int    tailMarker = remaining.indexOf('_');
			final String tail       = remaining.substring(0, tailMarker);

			remaining = PlaceholderAPI.setBracketPlaceholders(player, remaining.substring(tailMarker + 1));

			switch (params.substring(0, marker).toLowerCase())
			{
				case "percent":
					return generatePercent(head, tail, remaining);
				case "message":
					return generateMessage(head, tail, remaining);
				default:
					return null;
			}
		}
		catch (final IndexOutOfBoundsException ignored)
		{
			return null; // fuck it
		}
	}


	@Nullable
	private static String generatePercent(@NotNull final String head, @NotNull final String tail, @NotNull final String percent)
	{
		if (percent.length() > 3)
		{
			return null;
		}

		try
		{
			final int[] values = generatePercentageRGB(resolveDefinedHexInts(head, tail), Integer.parseInt(percent) / 100.0);

			return "§x§" + String.join("§", String.format("%02X%02X%02X", values[0], values[1], values[2]).split(""));
		}
		catch (final NumberFormatException ignored)
		{
			return null;
		}
	}

	@NotNull
	private static String generateMessage(@NotNull final String head, @NotNull final String tail, @NotNull final String message)
	{
		if (message.isEmpty())
		{
			return message;
		}

		final int[] hex = resolveDefinedHexInts(head, tail);

		final double        perChar = 100.0 / message.length();
		final StringBuilder builder = new StringBuilder();

		double current = 0;

		for (final char c : message.toCharArray())
		{
			final int[] values = generatePercentageRGB(hex, current / 100.0);

			builder.append("§x§")
				   .append(String.join("§", String.format("%02X%02X%02X", values[0], values[1], values[2]).split("")))
				   .append(c);

			current = Math.min(100.0, current + perChar);
		}

		return builder.toString();
	}


	/**
	 * Create an array of ints representing the rgb values of the provided strings
	 *
	 * @param head The head color of the gradient
	 * @param tail The tail color of the gradient
	 *
	 * @return An array of 6 ints between [0..255]
	 *
	 * @apiNote Array values are in order [hexOneR, hexOneG, hexOneB, hexTwoR, hexTwoG, hexTwoB]
	 */
	@NotNull
	private static int[] resolveDefinedHexInts(@NotNull final String head, @NotNull final String tail)
	{
		final int hexOneR = Integer.parseInt(head.substring(1, 3), 16);
		final int hexOneG = Integer.parseInt(head.substring(3, 5), 16);
		final int hexOneB = Integer.parseInt(head.substring(5, 7), 16);

		final int hexTwoR = Integer.parseInt(tail.substring(1, 3), 16);
		final int hexTwoG = Integer.parseInt(tail.substring(3, 5), 16);
		final int hexTwoB = Integer.parseInt(tail.substring(5, 7), 16);

		return new int[]{hexOneR, hexOneG, hexOneB, hexTwoR, hexTwoG, hexTwoB};
	}

	/**
	 * Create an array of ints representing the rgb value of the color at the <code>percentage</code> of the  gradient defined by <code>hex</code>
	 *
	 * @param hex        The gradient's hex values, created by {@link GradientExpansion#resolveDefinedHexInts(String, String)}
	 * @param percentage The percentage along the gradient to target
	 *
	 * @return An array of 3 ints between [0..255]
	 *
	 * @apiNote Array values are in order [valueR, valueG, valueB]
	 */
	@NotNull
	private static int[] generatePercentageRGB(@NotNull final int[] hex, final double percentage)
	{
		final int valueR = (int) (hex[0] + ((hex[3] - hex[0]) * percentage));
		final int valueG = (int) (hex[1] + ((hex[4] - hex[1]) * percentage));
		final int valueB = (int) (hex[2] + ((hex[5] - hex[2]) * percentage));

		return new int[]{valueR, valueG, valueB};
	}

}
