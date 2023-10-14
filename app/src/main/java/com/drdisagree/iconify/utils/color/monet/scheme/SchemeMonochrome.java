/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drdisagree.iconify.utils.color.monet.scheme;

import com.drdisagree.iconify.utils.color.monet.hct.Hct;
import com.drdisagree.iconify.utils.color.monet.palettes.TonalPalette;

/**
 * A monochrome theme, colors are purely black / white / gray.
 */
public class SchemeMonochrome extends DynamicScheme {
    public SchemeMonochrome(Hct sourceColorHct, boolean isDark, double contrastLevel) {
        super(
                sourceColorHct,
                Variant.MONOCHROME,
                isDark,
                contrastLevel,
                TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 0.0),
                TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 0.0),
                TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 0.0),
                TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 0.0),
                TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 0.0));
    }
}
