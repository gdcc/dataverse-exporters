#!/bin/sh
mlcroissant validate --jsonld src/test/resources/minimal/out/croissant.json
mlcroissant validate --jsonld src/test/resources/max/out/croissant.json
mlcroissant validate --jsonld src/test/resources/cars/out/croissant.json
