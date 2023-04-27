/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorBoolean;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.query.PrincipalExtended;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.DynamicContext;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class CheckNavLinkQuery implements ValidationCheck {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckNavLinkQuery.class);

    @ConfigurableField(editor = EditorString.class,
            label = "Target NavLink", description = "The navLink the query targets.")
    @EditorString.EdOptsString
    private String targetNavLink;

    @ConfigurableField(editor = EditorString.class,
            label = "Query", description = "The Query to use to find if the user can take the action.")
    @EditorString.EdOptsString
    private String query;

    @ConfigurableField(editor = EditorBoolean.class,
            label = "Empty Allowed", description = "Is the Navigation property allowed to be empty?")
    @EditorBoolean.EdOptsBool(dflt = true)
    private boolean emptyAllowed;

    private ResourcePath path;
    private EntityType entityType;
    private EntityType targetType;
    private NavigationPropertyMain targetNp;
    private DynamicContext context;
    private Query parsedQuery;

    @Override
    public boolean check(PostgresPersistenceManager pm, Entity contextEntity) {
        if (parsedQuery == null) {
            init(contextEntity, pm);
        }
        try {
            context.setEntity(contextEntity);
            context.setUser(PrincipalExtended.getLocalPrincipal());
            if (targetNp instanceof NavigationPropertyMain.NavigationPropertyEntity targetNpEntity) {
                final Entity targetEntity = contextEntity.getProperty(targetNpEntity);
                if (targetEntity == null) {
                    LOGGER.debug("  Check on {}.{} (empty): {}", entityType, targetNp, isEmptyAllowed());
                    return isEmptyAllowed();
                }
                final Entity result = pm.get(targetType, targetEntity.getId(), parsedQuery);
                final boolean valid = result != null;
                LOGGER.debug("  Check on {}.{}: {}", entityType, targetNp, valid);
                return valid;
            }
            if (targetNp instanceof NavigationPropertyMain.NavigationPropertyEntitySet targetNpEntitySet) {
                EntitySet targetEntities = contextEntity.getProperty(targetNpEntitySet);
                if (targetEntities == null || targetEntities.isEmpty()) {
                    LOGGER.debug("  Check on {}.{} (empty): {}", entityType, targetNp, isEmptyAllowed());
                    return isEmptyAllowed();
                }
                for (Entity te : targetEntities) {
                    Entity result = pm.get(targetType, te.getId(), parsedQuery);
                    if (result == null) {
                        LOGGER.debug("  Check on {}.{}({}): false", entityType, targetNp, te.getId());
                        return false;
                    }
                }
                LOGGER.debug("  Checks ({}) on {}.{}: true", targetEntities.size(), entityType, targetNp);
                return true;
            }
        } finally {
            context.clear();
        }
        return false;
    }

    private void init(Entity contextEntity, PostgresPersistenceManager pm) {
        entityType = contextEntity.getEntityType();
        targetNp = entityType.getNavigationProperty(getTargetNavLink());
        targetType = targetNp.getEntityType();
        final CoreSettings coreSettings = pm.getCoreSettings();
        final QueryDefaults queryDefaults = coreSettings.getQueryDefaults();
        path = new ResourcePath(queryDefaults.getServiceRootUrl(), Version.V_1_1, "/" + targetType.plural).addPathElement(new PathElementEntitySet(targetType));
        context = new DynamicContext();
        parsedQuery = QueryParser.parseQuery(getQuery(), coreSettings, path, PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL, context)
                .validate(targetType);
        LOGGER.info("Initialised check on {}.{}", entityType, targetNp);
    }

    public String getTargetNavLink() {
        return targetNavLink;
    }

    public void setTargetNavLink(String targetNavLink) {
        this.targetNavLink = targetNavLink;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isEmptyAllowed() {
        return emptyAllowed;
    }

    public CheckNavLinkQuery setEmptyAllowed(boolean emptyAllowed) {
        this.emptyAllowed = emptyAllowed;
        return this;
    }

    @Override
    public String toString() {
        return targetNavLink + ": " + query;
    }

}