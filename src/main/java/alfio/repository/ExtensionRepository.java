/**
 * This file is part of alf.io.
 *
 * alf.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * alf.io is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with alf.io.  If not, see <http://www.gnu.org/licenses/>.
 */

package alfio.repository;

import alfio.model.ExtensionSupport;
import ch.digitalfondue.npjt.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@QueryRepository
public interface ExtensionRepository {

    @Query("insert into extension_support(path, name, hash, enabled, async, script) values " +
        " (:path, :name, :hash, :enabled, :async, :script)")
    int insert(@Bind("path") String path,
               @Bind("name") String name,
               @Bind("hash") String hash,
               @Bind("enabled") boolean enabled,
               @Bind("async") boolean async,
               @Bind("script") String script);

    @Query("update extension_support set hash = :hash, enabled = :enabled, async = :async, script = :script where path = :path and name = :name")
    int update(@Bind("path") String path,
               @Bind("name") String name,
               @Bind("hash") String hash,
               @Bind("enabled") boolean enabled,
               @Bind("async") boolean async,
               @Bind("script") String script);

    @Query("update extension_support set enabled = :enabled where path = :path and name = :name")
    int toggle(@Bind("path") String path, @Bind("name") String name, @Bind("enabled") boolean enabled);

    @Query("insert into extension_event(es_id_fk, event) values " +
        " (:extensionId, :event)")
    int insertEvent(@Bind("extensionId") int extensionId, @Bind("event") String event);

    @Query("select es_id from extension_support where path = :path and name = :name")
    int getExtensionIdFor(@Bind("path") String name, @Bind("name") String path);

    @Query("select script from extension_support where path = :path and name = :name")
    String getScript(@Bind("path") String path, @Bind("name") String name);

    @Query("select script from extension_support where path = :path and name = :name")
    Optional<String> getMaybeScript(@Bind("path") String path, @Bind("name") String name);

    @Query("select * from extension_support where path = :path and name = :name")
    Optional<ExtensionSupport> getSingle(@Bind("path") String path, @Bind("name") String name);

    @Query("delete from extension_event where es_id_fk = (select es_id from extension_support where path = :path and name = :name)")
    int deleteEventsForPath(@Bind("path") String path, @Bind("name") String name);

    @Query("delete from extension_support where path = :path and name = :name")
    int deleteScriptForPath(@Bind("path") String path, @Bind("name") String name);

    @Query("select * from extension_support order by name, path")
    List<ExtensionSupport> listAll();

    @Query("select a3.es_id, a3.path, a3.name, a3.hash from " +
        " (select a1.* from " +
        " (select es_id, path, name, hash from extension_support where enabled = true and async = :async and (path in (:possiblePaths))) a1 " +
        " left outer join (select es_id, path, name from extension_support where enabled = true and async = :async and (path in (:possiblePaths))) a2 on " +
        " (a1.es_id = a2.es_id) and length(a1.path) < length(a2.path) where a2.path is null) a3 " +
        " " +
        " inner join extension_event on es_id_fk = a3.es_id where event = :event order by a3.name, a3.path")
    List<ExtensionSupport.ScriptPathNameHash> findActive(@Bind("possiblePaths") Set<String> possiblePaths,
                                                         @Bind("async") boolean async,
                                                         @Bind("event") String event);

    @Query("delete from extension_configuration_metadata where  ecm_es_id_fk = :extensionId")
    int deleteExtensionParameter(@Bind("extensionId") int extensionId);

    @Query("insert into extension_configuration_metadata(ecm_es_id_fk, ecm_name, ecm_description, ecm_type, ecm_configuration_level, ecm_mandatory) " +
        " values (:extensionId, :name, :description, :type, :configurationLevel, :mandatory)")
    int registerExtensionConfigurationMetadata(@Bind("extensionId") int extensionId,
                                               @Bind("name") String name,
                                               @Bind("description") String description,
                                               @Bind("type") String type,
                                               @Bind("configurationLevel") String configurationLevel,
                                               @Bind("mandatory") boolean mandatory);


    @Query("select ecm_id, ecm_name, ecm_configuration_level, ecm_description, ecm_type, ecm_mandatory, path, es_id, name, conf_path, conf_value"+
        " from extension_configuration_metadata " +
        " inner join extension_support on es_id = ecm_es_id_fk " +
        " left outer join extension_configuration_metadata_value on ecm_id = fk_ecm_id " +
        " where ecm_configuration_level = :configurationLevel and (path in (:possiblePaths) or path like :pathPattern) order by es_id, name, ecm_id, ecm_name")
    List<ExtensionSupport.ExtensionParameterMetadataAndValue> getParametersForLevelAndPath(
        @Bind("configurationLevel") String configurationLevel,
        @Bind("possiblePaths") Set<String> possiblePaths,
        @Bind("pathPattern") String pathPattern);

}
