package ru.mirari.infra.coffee

import grails.converters.JSON

class KnockCoffeeService {

    static transactional = false

    Map toMap(domainObject) {
        if(!domainObject) return null
        Map result = [:]

        CoffeeDomain conf = CoffeeDomain.getConfig(domainObject.class)

        conf.watchedProperties.each {String name ->
            def value = domainObject."${name}"
            CoffeeProperty prop = conf.property(name)
            if(prop.toJson) {
                value = prop.toJson(value)
            } else if(prop.toMany) {
                value = ((List)value).collect {toMap(it)}
            } else if(prop.toOne) {
                value = toMap(value)
            }
            result.put(name, value)
        }
        result
    }

    JSON toJson(domainObject) {
        toMap(domainObject) as JSON
    }

    def fromJson(String json, Class domainClass) {
        def element = JSON.parse(json)
        if(element instanceof Map) {
            def domain
            domain = element.id ? domainClass.get(element.id) : domainClass.newInstance()
            fromJson((Map)element, domain)
            return domain
        }
    }

    def fromJson(String json, domain) {
        def element = JSON.parse(json)
        if(element instanceof Map) {
            fromJson((Map)element, domain)
        }
        domain
    }

    def fromJson(Map json, domain) {
        CoffeeDomain conf = CoffeeDomain.getConfig(domain.class)

        Map props = [:]

        conf.watchedProperties.each {String name ->
            def value = json."${name}"
            CoffeeProperty prop = conf.property(name)
            if(prop.fromJson) {
                value = prop.fromJson(value, domain)
            } else if(prop.toMany) {
                value = ((List)value).collect {fromJson((Map)it, prop.association)}
            } else if(prop.toOne) {
                value = fromJson((Map)value, prop.association)
            }
            props.put(name, value)
        }
        domain.properties = props
    }
}
