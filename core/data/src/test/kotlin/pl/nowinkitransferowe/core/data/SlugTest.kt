/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.nowinkitransferowe.core.data

import org.junit.Test
import pl.nowinkitransferowe.core.model.slugOf
import kotlin.test.assertEquals

class SlugTest {
    @Test
    fun slugs_areLikeTheWebsites() {
        assertEquals("bayern-monachium", slugOf("Bayern Monachium"))
        assertEquals("lukasz-fabianski", slugOf("Łukasz Fabiański"))
        assertEquals("djylian-n-guessan", slugOf("Djylian N'Guessan"))
        assertEquals("fermin-lopez", slugOf("Fermín López"))
        assertEquals("polacy-za-granica", slugOf("Polacy za granicą"))
        assertEquals("fc-porto", slugOf(" FC Porto "))
    }
}
