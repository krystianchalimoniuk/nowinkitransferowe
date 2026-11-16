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

package pl.nowinkitransferowe.core.testing.rules

import android.util.Log
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.junit.runners.model.TestTimedOutException
import java.util.concurrent.TimeUnit

/**
 * Fails a test that runs longer than [minutes] instead of letting it hang the whole run. On a
 * timeout it logs the stack of every thread (tag `HangTimeoutRule`) and adds the main thread's
 * stack to the failure, because a stuck UI test usually waits for the main thread to go idle.
 *
 * Use it as the outermost rule so it also covers launching and closing the activity.
 */
class HangTimeoutRule(private val minutes: Long = 3) : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        val timeout = Timeout.builder()
            .withTimeout(minutes, TimeUnit.MINUTES)
            .withLookingForStuckThread(true)
            .build()
            .apply(base, description)
        return object : Statement() {
            override fun evaluate() {
                try {
                    timeout.evaluate()
                } catch (e: TestTimedOutException) {
                    val stacks = Thread.getAllStackTraces()
                    Log.e(TAG, "${description.displayName} timed out after $minutes min; thread dump:")
                    stacks.forEach { (thread, stack) ->
                        Log.e(TAG, "\"${thread.name}\" ${thread.state}\n" + stack.joinToString("\n") { "    at $it" })
                    }
                    stacks.entries.firstOrNull { it.key.name == "main" }?.let { (thread, stack) ->
                        e.addSuppressed(StuckThreadException(thread, stack))
                    }
                    throw e
                }
            }
        }
    }

    private class StuckThreadException(thread: Thread, stack: Array<StackTraceElement>) :
        Exception("Stack of thread \"${thread.name}\" (${thread.state}) at the timeout") {
        init {
            stackTrace = stack
        }
    }

    private companion object {
        const val TAG = "HangTimeoutRule"
    }
}
