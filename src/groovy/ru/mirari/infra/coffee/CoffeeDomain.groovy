package ru.mirari.infra.coffee

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

/**
 * @author alari
 * @since 8/12/12 12:16 AM
 *
 * static coffeeIgnore = []
 * static coffeeExtend = ""
 * static coffee = false
 * static coffee = [propName:(Map)propConfig]
 */
class CoffeeDomain {
    String extend
    private Map<String,CoffeeProperty> props = [:]

    private List<String> ignore = []

    final private DefaultGrailsDomainClass domainClass
    final Class clazz

    static Map<Class, CoffeeDomain> configs = [:]

    static CoffeeDomain getConfig(final Class domainClass, final DefaultGrailsDomainClass grailsDomainClass = null) {
        if (!configs.containsKey(domainClass)) {
            synchronized (CoffeeDomain) {
                if (!configs.containsKey(domainClass)) {
                    configs.put(domainClass, new CoffeeDomain(grailsDomainClass ?: new DefaultGrailsDomainClass(domainClass)))
                }
            }
        }
        configs.get(domainClass)
    }

    static CoffeeDomain getConfig(final DefaultGrailsDomainClass domainClass) {
        Class clazz = domainClass.getClazz()
        getConfig(clazz, domainClass)
    }

    private CoffeeDomain(final DefaultGrailsDomainClass domainClass) {
        def dCoffee = domainClass.getStaticPropertyValue("coffee", Object)
        if (false.equals(dCoffee)) return;
        Map<String,Map> domainCoffee = (Map) dCoffee ?: [:]

        ignore = domainClass.getStaticPropertyValue("coffeeIgnore", List) ?: []

        for(GrailsDomainClassProperty property : domainClass.properties) {
            if(ignore.contains(property.name)) continue;
            props.put(property.name, new CoffeeProperty(property))
        }

        extend = domainClass.getStaticPropertyValue("coffeeExtend", String) ?: ""

        domainCoffee.keySet().each {k->
            props.get(k)?.setCoffeeConfig(domainCoffee.get(k))
        }

        this.domainClass = domainClass
        clazz = domainClass.clazz
    }

    CoffeeProperty property(final String name) {
        props.get(name)
    }

    Set<String> getWatchedProperties() {
        props.keySet()
    }
}
