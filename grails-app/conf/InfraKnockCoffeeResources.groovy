modules = {
    knockout {
        resource url: "/js/ko/knockout-2.1.0.js"
    }
    knockout_mapping {
        resource url: "/js/ko/knockout-mapping.2.3.0.js"
        dependsOn "knockout"
    }
}