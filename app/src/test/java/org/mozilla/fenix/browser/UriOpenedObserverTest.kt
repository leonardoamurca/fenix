/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.browser

import androidx.lifecycle.LifecycleOwner
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import org.junit.Before
import org.junit.Test
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.search.telemetry.ads.AdsTelemetry
import org.mozilla.fenix.utils.Settings

class UriOpenedObserverTest {

    private val settings: Settings = mockk(relaxed = true)
    private val owner: LifecycleOwner = mockk(relaxed = true)
    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val metrics: MetricController = mockk()
    private val ads: AdsTelemetry = mockk()
    private lateinit var observer: UriOpenedObserver

    @Before
    fun setup() {
        observer = UriOpenedObserver(settings, owner, sessionManager, metrics, ads)
    }

    @Test
    fun `registers self as observer`() {
        verify { sessionManager.register(observer, owner) }
    }

    @Test
    fun `registers single session observer`() {
        val session: Session = mockk(relaxed = true)

        observer.onSessionAdded(session)
        verify { session.register(observer.singleSessionObserver, owner) }

        observer.onSessionSelected(session)
        verify { session.register(observer.singleSessionObserver, owner) }

        observer.onSessionRemoved(session)
        verify { session.unregister(observer.singleSessionObserver) }
    }

    @Test
    fun `registers when all sessions are restored`() {
        val session1: Session = mockk(relaxed = true)
        val session2: Session = mockk(relaxed = true)
        every { sessionManager.sessions } returns listOf(session1, session2)

        observer.onSessionsRestored()

        verify { session1.register(observer.singleSessionObserver, owner) }
        verify { session2.register(observer.singleSessionObserver, owner) }
    }

    @Test
    fun `unregisters when all sessions are removed`() {
        val session1: Session = mockk(relaxed = true)
        val session2: Session = mockk(relaxed = true)
        every { sessionManager.sessions } returns listOf(session1, session2)

        observer.onAllSessionsRemoved()

        verify { session1.unregister(observer.singleSessionObserver) }
        verify { session2.unregister(observer.singleSessionObserver) }
    }
}
