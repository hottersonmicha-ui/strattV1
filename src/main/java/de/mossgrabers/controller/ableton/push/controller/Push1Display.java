// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.ableton.push;

import de.mossgrabers.framework.controller.display.AbstractTextDisplay;
import de.mossgrabers.framework.controller.display.Format;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.utils.Pair;
import de.mossgrabers.framework.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The display of Push 1.
 *
 * @author Jürgen Moßgraber
 */
public class Push1Display extends AbstractTextDisplay
{
    public static final String BARS_NON = Character.toString ((char) 6);
    public static final String BARS_ONE = Character.toString ((char) 3);
    public static final String BARS_TWO = Character.toString ((char) 5);
    public static final String BARS_ONE_L = Character.toString ((char) 4);
    private static final String NON_4 = BARS_NON + BARS_NON + BARS_NON + BARS_NON;
    public static final String SELECT_ARROW = Character.toString ((char) 127);
    public static final String DEGREE = Character.toString ((char) 9);
    public static final String RIGHT_ARROW = Character.toString ((char) 30);
    public static final String FOLDER = Character.toString ((char) 7);
    public static final String THREE_ROWS = Character.toString ((char) 2);
    public static final String DIVISION = Character.toString ((char) 24);

    private static final String [] SYSEX_MESSAGE =
    {
        "F0 47 7F 15 18 00 45 00 ",
        "F0 47 7F 15 19 00 45 00 ",
        "F0 47 7F 15 1A 00 45 00 ",
        "F0 47 7F 15 1B 00 45 00 "
    };

    private final int maxParameterValue;

    public Push1Display (final IHost host, final int maxParameterValue, final IMidiOutput output, final PushConfiguration configuration)
    {
        super (host, output, 4, 8, 68);
        this.maxParameterValue = maxParameterValue;
    }

    @Override
    public void shutdown ()
    {
        this.clear ().setBlock (1, 1, "     Please start").setBlock (1, 2, this.host.getName () + " to play...").allDone ().flush ();
    }

    @Override
    public ITextDisplay clearCell (final int row, final int column)
    {
        this.cells[row * this.noOfCells + column] = column % 2 == 0 ? "         " : "        ";
        return this;
    }

    @Override
    public ITextDisplay setBlock (final int row, final int block, final String value)
    {
        final int cell = 2 * block;
        if (value.length () > 9)
        {
            this.cells[row * 8 + cell] = value.substring (0, 9);
            this.cells[row * 8 + cell + 1] = StringUtils.pad (value.substring (9), 8, ' ');
        }
        else
        {
            this.cells[row * 8 + cell] = StringUtils.pad (value, 9, ' ');
            this.clearCell (row, cell + 1);
        }
        return this;
    }

    @Override
    public ITextDisplay setCell (final int row, final int cell, final int value, final Format format)
    {
        return this.setCell (row, cell, formatStr (value, format, this.maxParameterValue));
    }

    @Override
    public ITextDisplay setCell (final int row, final int cell, final String value)
    {
        this.cells[row * 8 + cell] = StringUtils.pad (value, 8, ' ') + (cell % 2 == 0 ? " " : "");
        return this;
    }

    @Override
    protected String convertCharacterset (final String text)
    {
        final String t = text.replace (Push1Display.BARS_NON, "-").replace (Push1Display.BARS_ONE, "|").replace (Push1Display.BARS_TWO, "|").replace (Push1Display.BARS_ONE_L, "|");
        final StringBuilder sb = new StringBuilder (t.length ());
        int beginIndex = 0;
        int endIndex = 17;
        for (int i = 0; i < 4; i++)
        {
            sb.append (t.substring (beginIndex, endIndex));
            beginIndex += 17;
            endIndex += 17;
            if (i != 3)
                sb.append (' ');
        }
        return sb.toString ();
    }

    @Override
    public void writeLine (final int row, final String text, final String previousText)
    {
        this.output.sendSysex (Push1Display.SYSEX_MESSAGE[row] + StringUtils.asciiToHex (text) + "F7");
    }

    private static String formatStr (final int value, final Format format, final int maxParam)
    {
        switch (format)
        {
            case FORMAT_VALUE:
                return formatValue (value, maxParam);
            case FORMAT_PAN:
                return formatPan (value, maxParam);
            default:
                return Integer.toString (value);
        }
    }

    public static String formatValue (final int value, final int maxParam)
    {
        final StringBuilder n = new StringBuilder ();
        Arrays.asList (fillFields (value, maxParam)).forEach (n::append);
        return n.toString ();
    }

    public static String formatValue (final int modulated, final int value, final int maxParam)
    {
        final String [] fields = fillFields (value, maxParam);
        final int noOfBarsModulated = (int) Math.round (16.0 * modulated / maxParam);
        final int pos = noOfBarsModulated / 2;
        if (noOfBarsModulated % 2 == 0)
        {
            if (pos > 0)
            {
                if (fields[pos - 1].charAt (0) == BARS_NON.charAt (0))
                    fields[pos - 1] = BARS_ONE_L;
                else if (fields[pos].charAt (0) == BARS_ONE.charAt (0))
                    fields[pos - 1] = BARS_TWO;
            }
        }
        else
        {
            if (fields[pos].charAt (0) == BARS_NON.charAt (0))
                fields[pos] = BARS_ONE;
            else
                fields[pos] = BARS_TWO;
        }

        final StringBuilder n = new StringBuilder ();
        Arrays.asList (fields).forEach (n::append);
        return n.toString ();
    }

    private static String [] fillFields (final int value, final int maxParam)
    {
        final String [] fields = new String [8];
        Arrays.fill (fields, Push1Display.BARS_NON);

        final int noOfBars = (int) Math.round (16.0 * value / maxParam);
        int count;
        for (count = 0; count < noOfBars / 2; count++)
            fields[count] = Push1Display.BARS_TWO;
        if (noOfBars % 2 == 1)
            fields[count] = Push1Display.BARS_ONE;

        return fields;
    }

    private static String formatPan (final int pan, final int maxParam)
    {
        final int middle = maxParam / 2;
        if (pan == middle)
            return Push1Display.NON_4 + Push1Display.NON_4;
        final boolean isLeft = pan < middle;
        final int pos = isLeft ? middle - pan : pan - middle;
        final int noOfBars = 16 * pos / maxParam;
        StringBuilder n = new StringBuilder ();
        for (int i = 0; i < noOfBars / 2; i++)
            n.append (BARS_TWO);
        if (pan >= maxParam - 1)
            n.append (BARS_TWO);
        else if (noOfBars % 2 == 1)
            n.append (isLeft ? Push1Display.BARS_ONE_L : Push1Display.BARS_ONE);
        n = new StringBuilder (Push1Display.NON_4).append (StringUtils.pad (n.toString (), 4, Push1Display.BARS_NON.charAt (0)));
        return isLeft ? n.reverse ().toString () : n.toString ();
    }

    public static List<Pair<String, Boolean>> createMenuList (final int displaySize, final String [] elements, final int selectedIndex)
    {
        final List<Pair<String, Boolean>> menu = new ArrayList<> ();
        final int startIndex = Math.clamp (selectedIndex, 0, Math.max (elements.length - displaySize, 0));
        for (int i = 0; i < displaySize; i++)
        {
            final int pos = startIndex + i;
            final String itemName = pos < elements.length ? elements[pos] : "";
            menu.add (new Pair<> (itemName, Boolean.valueOf (pos == selectedIndex)));
        }
        return menu;
    }

    @Override
    protected void updateNotification ()
    {
        final int blockLength = 17;
        final String trimmed = this.notificationMessage.trim ();
        final int length = trimmed.length ();
        if (length > blockLength)
        {
            super.updateNotification ();
            return;
        }

        String text = this.currentMessage[0].substring (0, this.noOfCharacters - blockLength) + StringUtils.pad (trimmed, blockLength);
        this.updateLine (0, text, this.currentMessage[0]);
        text = this.currentMessage[1].substring (0, this.noOfCharacters - blockLength) + StringUtils.pad ("", blockLength);
        this.updateLine (1, text, this.currentMessage[1]);
    }
}
