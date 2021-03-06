/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.PollChoice
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class PollChoiceUnitTest : Assert() {

    @Test
    fun testPollChoice() {
        val pollChoices: Array<PollChoice> = pollChoiceJSON.parse()

        Assert.assertNotNull(pollChoices)

        for (pollChoice in pollChoices) {
            Assert.assertNotNull(pollChoice)
            Assert.assertNotNull(pollChoice.text)
            Assert.assertTrue(pollChoice.position >= 0)
            Assert.assertTrue(pollChoice.id > 0)
        }
    }

    @Language("JSON")
    private var pollChoiceJSON = """
      [
        {
          "id": "762",
          "text": "Ghbb",
          "position": 0,
          "is_correct": false
        },
        {
          "id": "761",
          "text": "Nnnbbb",
          "position": 1,
          "is_correct": false
        }
      ]"""
}
