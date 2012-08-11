package ru.mirari.infra

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

/**
 * @author alari
 * @since 8/12/12 12:16 AM
 *
 * Map exampleConfig = [
 init: [fieldName: "in constructor thing"],
 extend: "class name to extend",
 ignore: ["field1", "field2"],
 mapping: [
 fieldName: [
 key: "method with o input",
 create: "create method with o input",
 update: "update method with o input"
 ]
 ]
 ]
 */
class CoffeeDomainConfig {
    String extend
    List<String> ignore = []
    Map<String, String> init = [:]
    Map<String,Map<String,String>> mapping = [:]

    final private DefaultGrailsDomainClass domainClass

    final List<String> watchedProperties

    static Map<Class,CoffeeDomainConfig> configs = [:]

    static CoffeeDomainConfig getConfig(final Class domainClass, final DefaultGrailsDomainClass grailsDomainClass = null) {
        if(!configs.containsKey(domainClass)) {
            synchronized(CoffeeDomainConfig) {
                if(!configs.containsKey(domainClass)) {
                    configs.put(domainClass, new CoffeeDomainConfig(grailsDomainClass ?: new DefaultGrailsDomainClass(domainClass)))
                }
            }
        }
        configs.get(domainClass)
    }

    static CoffeeDomainConfig getConfig(final DefaultGrailsDomainClass domainClass) {
        Class clazz = domainClass.getClazz()
        getConfig(clazz, domainClass)
    }

    private CoffeeDomainConfig(final DefaultGrailsDomainClass domainClass) {
        def dCoffee = domainClass.getStaticPropertyValue("coffee", Object)
        if (false.equals(dCoffee)) return;
        Map domainCoffee = (Map) dCoffee ?: [:]

        extend = domainCoffee.extend ?: ""

        ignore = domainCoffee.ignore ?: []

        mapping = domainCoffee.mapping ?: [:]
        init = domainCoffee.init ?: [:]

        this.domainClass = domainClass

        watchedProperties = domainClass.properties*.name - ignore
    }

    String getExtendCoffee() {
        extend ? " extends ${extend}" : ""
    }

    String getPropertyInitCoffee(final String name) {
        if (name in ignore) return;
        String propCoffee = "@$name = "
        if (init.get(name)) {
            propCoffee += init.get(name)
        } else {
            final GrailsDomainClassProperty prop = domainClass.getPropertyByName(name)
            if (prop.isOneToMany() || prop.isManyToMany() || prop.isBasicCollectionType()) {
                propCoffee += "ko.observableArray []"
            } else {
                propCoffee += "ko.observable null"
            }
        }
        propCoffee
    }

    boolean isWatchedManyRelation(final String name) {
        if(!watchedProperties.contains(name)) return false;
        def prop = domainClass.getPropertyByName(name)
        return prop.isOneToMany() || prop.isManyToMany()
    }

    boolean isWatchedOneRelation(final String name) {
        if(!watchedProperties.contains(name)) return false;
        def prop = domainClass.getPropertyByName(name)
        return prop.isOneToMany() || prop.isManyToMany()
    }
}
