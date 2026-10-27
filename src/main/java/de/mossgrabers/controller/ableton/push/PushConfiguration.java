// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.ableton.push;

/**
 * Minimal Push 1 configuration class extracted from the upstream DrivenByMoss codebase.
 *
 * This file keeps the relevant Push 1 config surface without including the full
 * controller framework used by the complete project.
 */
public class PushConfiguration
{
    private final PushVersion pushVersion;
    private int velocityCurvePush1 = 1;
    private int padThresholdPush1 = 20;

    public PushConfiguration (final PushVersion pushVersion)
    {
        this.pushVersion = pushVersion;
    }

    public PushVersion getPushVersion ()
    {
        return this.pushVersion;
    }

    public boolean isPushModern ()
    {
        return this.pushVersion != PushVersion.VERSION_1;
    }

    public int getVelocityCurve ()
    {
        return this.velocityCurvePush1;
    }

    public void setVelocityCurve (final int velocityCurve)
    {
        this.velocityCurvePush1 = velocityCurve;
    }

    public int getPadThresholdPush1 ()
    {
        return this.padThresholdPush1;
    }

    public void setPadThresholdPush1 (final int padThreshold)
    {
        this.padThresholdPush1 = padThreshold;
    }
}
