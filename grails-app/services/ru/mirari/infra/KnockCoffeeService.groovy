package ru.mirari.infra

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement

class KnockCoffeeService {

    static transactional = false

    Map toMap(domainObject) {
        Map result = [:]

        CoffeeDomainConfig conf = CoffeeDomainConfig.getConfig(domainObject.class)

        conf.watchedProperties.each {String name ->
            def value = domainObject."${name}"
            if(conf.isWatchedManyRelation(name)) {
                value = ((List)value).collect {toMap(it)}
            } else if(conf.isWatchedOneRelation(name)) {
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
        JSONElement element = JSON.parse(json)
        // TODO: parse, create domain class, and so on
        // TODO: notice that it may be not a string
    }

    def fromJson(String json, domain) {
        domain.properties = JSON.parse(json)
    }
}
