/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.attachment.v1;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

/**
 * An attachment allows "attaching" arbitrary data to various game objects (entities, block entities, worlds and chunks at the moment).
 * Use the methods provided in {@link AttachmentRegistry} to create and register attachments. Attachments can
 * optionally be made to persist between restarts using a provided {@link Codec}.
 *
 * <p>While the API places no restrictions on the types of data that can be attached, it is generally encouraged to use
 * immutable types. More generally, different attachments <i>must not</i> share mutable state, and it is <i>strongly advised</i>
 * for attachments not to hold internal references to their target. See the following note on entity targets.</p>
 *
 * <p>Note on {@link Entity} targets: in several instances, the name needs to copy data from one {@link Entity} to another.
 * These are player respawning, mob conversion, return from the End and cross-world entity teleportation. By default,
 * attachments are simply copied wholesale, up to {@link #copyOnDeath()}. Since one entity instance is discarded,
 * an attachment that keeps a reference to an {@link Entity} instance can and will break unexpectedly. If,
 * for whatever reason, keeping to reference to the target entity is absolutely necessary, be sure to use
 * {@link ServerPlayerEvents#COPY_FROM}, {@link ServerEntityWorldChangeEvents#AFTER_ENTITY_CHANGE_WORLD}
 * and a mixin into {@link MobEntity#convertTo(EntityType, boolean)} to implement custom copying logic.</p>
 *
 * @param <A> type of the attached data. It is encouraged for this to be an immutable type.
 */
@ApiStatus.NonExtendable
@ApiStatus.Experimental
public interface AttachmentType<A> {
	/**
	 * @return the identifier that uniquely identifies this attachment
	 */
	Identifier identifier();

	/**
	 * An optional {@link Codec} used for reading and writing attachments to NBT for persistence.
	 *
	 * @return the persistence codec, may be null
	 */
	@Nullable
	Codec<A> persistenceCodec();

	/**
	 * @return whether the attachments persist across server restarts
	 */
	default boolean isPersistent() {
		return persistenceCodec() != null;
	}

	/**
	 * If an object has no value associated to an attachment,
	 * this initializer is used to create a non-{@code null} starting value.
	 *
	 * <p>It is <i>encouraged</i> for {@link A} to be an immutable data type, such as a primitive type
	 * or an immutable record.</p>
	 *
	 * <p>Otherwise, one must be very careful, as attachments <i>must not share any mutable state</i>.
	 * As an example, for a (mutable) list/array attachment type,
	 * the initializer should create a new independent instance each time it is called.</p>
	 *
	 * @return the initializer for this attachment
	 */
	@Nullable
	Supplier<A> initializer();

	/**
	 * @return whether the attachments should persist after an entity dies, for example when a player respawns or
	 * when a mob is converted (e.g. zombie → drowned)
	 */
	boolean copyOnDeath();
}
