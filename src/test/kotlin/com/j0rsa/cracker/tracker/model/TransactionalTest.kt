package com.j0rsa.cracker.tracker.model

import arrow.core.Either
import com.j0rsa.cracker.tracker.NotFound
import com.j0rsa.cracker.tracker.TrackerError
import com.j0rsa.cracker.tracker.TransactionManager
import com.j0rsa.cracker.tracker.repository.UserRepository
import org.junit.jupiter.api.BeforeAll
import kotlin.properties.Delegates

internal interface TransactionalTest {
	fun <T> isNotFound(result: Either<TrackerError, T>) =
		(result as Either.Left).a == NotFound

	fun tempTx(block: () -> Unit) =
		TransactionManager.tx {
			block()
			currentTransaction().rollback()
		}

	fun tempTxWithoutRollback(block: () -> Unit) =
		TransactionManager.tx {
			block()
		}

	fun currentTransaction() = TransactionManager.currentTransaction()

	companion object {
		lateinit var user: User
		lateinit var telegramUser: User
		var userId: UserId by Delegates.notNull()

		@BeforeAll
		@JvmStatic
		fun beforeAll() {
//            TransactionManager.migrate()
			TransactionManager.tx {
				user = User.find { Users.telegramId.isNull() }.firstOrNull() ?: defaultUser()
				telegramUser = UserRepository.findOneByTelegramId(1).firstOrNull() ?: defaultTelegramUser()
				userId = user.idValue()
			}
		}

//        @AfterAll
//        @JvmStatic
//        fun afterAll() {
//            TransactionManager.tx {
//                dropSchema()
//            }
//        }
	}
}