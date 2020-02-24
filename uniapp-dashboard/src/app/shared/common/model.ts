import * as _ from "lodash";

export class Model {

  static objectFactory(type: string) {
    switch (type) {
      //   case 'app-config':
      //     return new AppConfig();
      //   case 'super-app-config': return new SuperAppConfig();
    }
    return {};
  }

  static deserialize(jsonApi) {
    let r: any;
    if (_.isArray(jsonApi.data)) {
      r = _.map(jsonApi.data, (d) => {
        return _.extend(
          Model.createObject(d.type, Model.extractAttributes(d)),
          Model.extractRelationships(d, jsonApi)
        );
      });
    } else {
      r = _.extend(
        Model.createObject(jsonApi.data.type, Model.extractAttributes(jsonApi.data, jsonApi)),
        Model.extractRelationships(jsonApi.data, jsonApi)
      );
    }

    return r;
  }

  static extractAttributes(from: any, jsonApi: string = ''): any {
    let attr = from.attributes || {};
    if ('id' in from) {
      attr.id = from.id;
    }

    Object.keys(attr).map((key: string) => {
      if (_.isArray(attr[key])) {
        attr[key] = attr[key].map(obj => {
          if (obj && obj.type) {
            return Model.createObject(obj.type, Model.extractAttributes(obj));
          }
          return obj;
        });
      } else {
        if (attr[key] && attr[key].type) {
          attr[key] = Model.createObject(attr[key].type, Model.extractAttributes(attr[key]));
        }
      }
    });

    return attr;
  }

  static extractRelationships(from: any, jsonApi: any): any {

    if (!from.relationships) return null;

    let dest: any = {};
    Object.keys(from.relationships).map((key: string) => {
      let relationship = from.relationships[key];

      if (relationship.data === null) {
        return dest[key] = null;
      }

      if (_.isArray(relationship.data)) {
        let includes = relationship.data.map((relationshipData: Array<any>) => {
          return Model.extractIncludes(relationshipData, jsonApi);
        });
        if (includes) {
          dest[key] = includes;
        }
      } else {
        let includes = Model.extractIncludes(relationship.data, jsonApi);
        dest[key] = includes;
      }
    });
    return dest;
  }

  static extractIncludes(relationshipData: any, jsonApi: any) {

    if (!relationshipData) {
      return;
    }

    let included = _.find(jsonApi.included, {
      id: relationshipData.id,
      type: relationshipData.type
    });

    if (included) {
      // fetch included relations of this object too.
      return _.extend(
        Model.createObject(included.type, Model.extractAttributes(included)),
        // Model.extractRelationships(included, jsonApi)
      )
    } else {
      // not included
      // return Model.createObject(relationshipData.type, relationshipData);
      return null;
    }
  }

  static createObject(type: string, attributes: any): any {
    if (!type || !attributes) {
      return null;
    }
    return _.extend(Model.objectFactory(type), attributes);
  }

  /**
   * Converts filters into jsonApi format
   * @param filters 
   */
  static extractFiltersQuery(filters: any) {
    let filterQuery = [];
    Object.keys(filters).map(filterKey => {
      let filterType = filters[filterKey];
      if (!_.isArray(filterType.filters) || filterType.filters.length == 0) {
        return;
      }
      if (filterType.operator) {
        filterQuery.push('filter[' + filterKey + '][operator]=' + filterType.operator);
      }
      filterType.filters.map(d => {
        let v = '';
        if (_.isFunction(filterType.value)) {
          //v = _.result(filterType, 'value', 'default');
          filterType.value.call(null, d)
            .map(d => filterQuery.push(d));

        } else {
          v = d[filterType.value];
          filterQuery.push('filter[' + filterKey + '][value][]=' + v);
        }

      });

    });
    return filterQuery.join('&');
  }
  static baDeserialize(jsonApi) {
    let r: any;
    if (_.isArray(jsonApi.data)) {
      r = _.map(jsonApi.data, (d) => {
        return _.extend(
          Model.baCreateObject(d.type, Model.baExtractAttributes(d)),
          // Model.extractRelationships(d, jsonApi)
        );
      });
    } else {
      r = _.extend(
        Model.baCreateObject(jsonApi.data.type, Model.baExtractAttributes(jsonApi.data, jsonApi)),
        // Model.extractRelationships(jsonApi.data, jsonApi)
      );
    }

    return r;
  }

  static baExtractAttributes(from: any, jsonApi: string = ''): any {

    let attr = from || {};
    if ('id' in from) {
      attr.id = from.id;
    }

    Object.keys(attr).map((key: string) => {
      if (_.isArray(attr[key])) {
        attr[key] = attr[key].map(obj => {
          if (obj && obj.type) {
            return Model.baCreateObject(obj.type, Model.baExtractAttributes(obj));
          }
          return obj;
        });
      } else {
        if (attr[key] && attr[key].type) {
          attr[key] = Model.baCreateObject(attr[key].type, Model.baExtractAttributes(attr[key]));
        }
      }
    });
    return attr;
  }
  static baCreateObject(type: string, attributes: any): any {
    if (!type || !attributes) {
      return null;
    }
    return _.extend(Model.objectFactory(type), attributes);
  }
}