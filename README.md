[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Sonar Stats](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query-mongodb&metric=alert_status)](https://sonarcloud.io/dashboard?id=win.doyto%3Adoyto-query-mongodb)
[![Code Lines](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query-mongodb&metric=ncloc)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query-mongodb&metric=ncloc)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=win.doyto%3Adoyto-query-mongodb&metric=coverage)](https://sonarcloud.io/component_measures?id=win.doyto%3Adoyto-query-mongodb&metric=coverage)

DoytoQueryMongoDB
---

## Introduction

DoytoQueryMongoDB is an ORM framework for MongoDB in Java. 

##  Quick tutorial with Spring Boot

This tutorial shows you how to create a CRUD web controller with Spring Boot and  `doyto-query-mongodb` to access [MongoDB](https://www.mongodb.com/).
The test data is from the MongoDB [query-documents](https://www.mongodb.com/docs/manual/tutorial/query-documents/).
Refer to GitHub for [complete code](https://github.com/doytowin/doyto-query-demo).
    
#### **`inventory.json`** 
```json
[
  {"item": "journal", "qty": 25, "size": {"h": 14, "w": 21, "uom": "cm"}, "status": "A"}, 
  {"item": "notebook", "qty": 50, "size": {"h": 8.5, "w": 11, "uom": "in"}, "status": "A"}, 
  {"item": "paper", "qty": 100, "size": {"h": 8.5, "w": 11, "uom": "in"}, "status": "D"},
  {"item": "planner", "qty": 75, "size": {"h": 22.85, "w": 30, "uom": "cm"}, "status": "D"}, 
  {"item": "postcard", "qty": 45, "size": {"h": 10, "w": 15.25, "uom": "cm"}, "status": "A"}
]
```

### 0. Environment Preparation

Use [start.spring.io](https://start.spring.io) to create a web project with spring boot 2 and  following dependencies:
- Lombok
- Validation
- Spring Web
- Embedded MongoDB Database

### 1. Introduce Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>win.doyto</groupId>
        <artifactId>doyto-query-mongodb</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>win.doyto</groupId>
        <artifactId>doyto-query-web</artifactId>
        <version>1.0.0</version>
    </dependency>
    ...
</dependencies>
```

### 2. Business Classes

```java
@Getter
@Setter
public class InventorySize {
    private Double h;
    private Double w;
    private String uom;
}
```

```java
@Getter
@Setter
@Entity(type = EntityType.MONGO_DB, database = "doyto", name = "c_inventory")
public class InventoryEntity extends MongoPersistable<String> {
    private String item;
    private Integer qty;
    private InventorySize size;
    private String status;
}
```

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeQuery implements NestedQuery {
    @JsonProperty("hLt")
    private Double hLt;
    private String uom;
}
```

```java
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryQuery extends PageQuery {
    private String itemContain;
    private String status;
    private SizeQuery size;
}
```

```java
@RestController
@RequestMapping("inventory")
public class InventoryController extends AbstractEIQController<InventoryEntity, String, InventoryQuery> {
}
```

```java
@SpringBootApplication
public class DemoApplication extends WebMvcConfigurerAdapter {
    @Generated
    public static void main(String[] args) {
        SpringApplication.run(DoytoQueryDemoApplication.class, args);
    }
}
```

### 3. Tests
```java
@AutoConfigureMockMvc
@SpringBootTest(properties = {"spring.mongodb.embedded.version=5.0.5"})
class InventoryMvcTest {
    @Resource
    protected MockMvc mockMvc;

    @BeforeAll
    static void beforeAll(@Autowired MockMvc mockMvc) throws Exception {
        InputStream is = MongoApplicationTest.class.getResourceAsStream("/inventory.json");
        String data = StreamUtils.copyToString(is, Charset.defaultCharset());
        mockMvc.perform(post("/inventory/").content(data).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void queryExamples() throws Exception {
        mockMvc.perform(get("/inventory/?itemContain=book"))
               .andExpect(jsonPath("$.data.total").value(1));
        
        mockMvc.perform(get("/inventory/?status=A"))
               .andExpect(jsonPath("$.data.total").value(3));

        mockMvc.perform(get("/inventory/?size.hLt=12&status=A"))
               .andExpect(jsonPath("$.data.total").value(2))
               .andExpect(jsonPath("$.data.list[*].item",
                                   containsInRelativeOrder("notebook", "postcard")));

        mockMvc.perform(get("/inventory/?size.uom=in"))
               .andExpect(jsonPath("$.data.total").value(2))
               .andExpect(jsonPath("$.data.list[*].item",
                                   containsInRelativeOrder("notebook", "paper")));
    }
}
```

## Versions

| Module                        | Snapshot                                                                                                                                     | Release                                                                                                        |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| doyto-query-mongodb           | [![mongodb-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-mongodb/)                           | [![mongodb-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-mongodb/)                     |
| doyto-query-mongodb-spring-tx | [![mongodb-spring-tx-snapshots-img]](https://oss.sonatype.org/content/repositories/snapshots/win/doyto/doyto-query-mongodb-spring-txcommon/) | [![mongodb-spring-tx-release-img]](https://search.maven.org/artifact/win.doyto/doyto-query-mongodb-spring-tx/) |


License
-------
This project is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

[mongodb-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-mongodb?color=blue&server=https%3A%2F%2Foss.sonatype.org
[mongodb-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-mongodb?color=brightgreen
[mongodb-spring-tx-snapshots-img]: https://img.shields.io/nexus/s/win.doyto/doyto-query-mongodb-spring-tx?color=blue&server=https%3A%2F%2Foss.sonatype.org
[mongodb-spring-tx-release-img]: https://img.shields.io/maven-central/v/win.doyto/doyto-query-mongodb-spring-tx?color=brightgreen
