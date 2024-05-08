mapSubField = function (v) {
    return "test"
}
mapField = function (r) {
    return function (v) {
        r[v.typeName] = typeof v.value === 'string' ? v.value : mapSubField(v.value); 
    }
}
res = {};
x.stream().forEach(mapField(res))