/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.gamedata;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;

/** Class that manages the data stores. */
public class DataStoreManager {

  /**
   * Persisted tag for the MemoryDataStore, as its the only data store currently supported it will
   * be the default, this may change in the future.
   */
  private static final String MEMORY_DATA_STORE_TYPE_NAME = "default";

  /** The memory data store. */
  private static final DataStore memoryDataStore = new MemoryDataStore();

  /**
   * Returns the default data store.
   *
   * @return the default data store.
   */
  public DataStore getDefaultDataStore() {
    return memoryDataStore;
  }

  /**
   * Returns the data transfer object representation of the data store.
   *
   * @return a {@code CompletableFuture} containing the data transfer object representation of the
   *     data store.
   */
  public CompletableFuture<DataStoreDto> toDto() {
    return CompletableFuture.supplyAsync(
        () -> {
          var builder = DataStoreDto.newBuilder();
          builder.setDataStoreType(MEMORY_DATA_STORE_TYPE_NAME);
          try {
            for (String propertyType : memoryDataStore.getPropertyTypes().get()) {
              for (String namespace : memoryDataStore.getPropertyNamespaces(propertyType).get()) {
                memoryDataStore.toDto(propertyType, namespace).thenAccept(builder::addData).get();
              }
            }
          } catch (InterruptedException | ExecutionException e) {
            throw new CompletionException(e.getCause());
          }
          return builder.build();
        });
  }

  /**
   * Returns all the {@link MD5Key} for assets in the data store.
   *
   * @return a {@code CompletableFuture} containing the {@link MD5Key} for assets in the data store.
   */
  public CompletableFuture<Set<MD5Key>> getAssets() {
    return memoryDataStore.getAssets();
  }

  /** Removes all the data from the data store. */
  public void clear() {
    memoryDataStore.clear();
  }
}
