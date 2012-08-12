package ru.mirari.infra.coffee

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

/**
 * @author alari
 * @since 8/12/12 6:29 PM
 */
class CoffeeProperty {
    Class association
    boolean toMany
    boolean toOne
    boolean basicCollection

    String init
    Map<String,String> mapping
    Closure toJson
    Closure fromJson

    CoffeeProperty(final GrailsDomainClassProperty property) {
        toMany = property.isOneToMany() || property.isManyToMany()
        toOne = property.isOneToOne() || property.isManyToOne()
        basicCollection = property.isBasicCollectionType()
        if(property.isAssociation()) association = property.referencedDomainClass?.class ?: property.type
    }

    void setCoffeeConfig(Map<String,?> config) {
        init = config.init

        mapping = (Map)config.mapping

        toJson = (Closure)config.toJson
        fromJson = (Closure)config.fromJson
    }
}
