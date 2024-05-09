flatten = function (result, value) {
    return function (key) {
        flattened = value.get(key);
        if (!flattened.keySet && flattened.size && flattened.size() === 1) {
            flattened = flattened.get(0);
        }
        if (flattened.keySet) {
            innerFlattened = new Map();
            flattened.keySet().forEach(flatten(innerFlattened, flattened));
            flattened = innerFlattened;
        }
        result.put(key, flattened);
    }
};

res = new Map();
x.keySet().forEach(flatten(res, x))