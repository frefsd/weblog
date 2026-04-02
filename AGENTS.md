# AGENTS.md - WeBlog Development Guide

## Project Overview

WeBlog is a full-stack blog system with Spring Boot 3.5.9 backend (Java 17) and Vue 3.2 frontend. Multi-module Maven project with前后端分离 architecture.

**Module dependency chain**: `WeBlog-server` → `WeBlog-common` → `WeBlog-pojo`

---

## Build / Run / Test Commands

### Backend (Maven)

```bash
# Build all modules (from project root)
mvn clean install

# Build only server module (auto-builds dependencies)
mvn clean package -pl WeBlog-server -am

# Run dev server with hot reload
cd WeBlog-server && mvn spring-boot:run

# Run a single test class
mvn test -Dtest=WeBlogServerApplicationTests -pl WeBlog-server

# Run a single test method
mvn test -Dtest=WeBlogServerApplicationTests#contextLoads -pl WeBlog-server

# Package as executable JAR
mvn clean package -pl WeBlog-server -am

# Run packaged JAR
java -jar WeBlog-server/target/WeBlog-server-0.0.1-SNAPSHOT.jar
```

### Frontend (Vite)

```bash
cd weblog-vue3

npm install          # Install dependencies
npm run dev          # Dev server (port 6066)
npm run build        # Production build
npm run preview      # Preview production build
```

### Notes
- **No ESLint/Prettier** configured — follow existing code patterns
- **No frontend tests** exist — no test infrastructure
- **Backend tests**: JUnit 5 + Spring Boot Test; only 1 stub test exists

---

## Backend Code Style (Java)

### Package Structure
```
com.blog
├── controller/admin|frontend   # Controllers
├── service / service/impl      # Service interfaces + implementations
├── mapper                      # MyBatis-Plus mappers
├── entity                      # (in WeBlog-pojo) Database entities
├── dto                         # (in WeBlog-pojo) Request DTOs
├── vo                          # (in WeBlog-pojo) Response VOs
├── config                      # (in WeBlog-common) Spring configs
├── exception                   # (in WeBlog-common) Exception handling
├── filter                      # (in WeBlog-common) Security filters
├── result                      # (in WeBlog-common) Result/PageResult
└── utils                       # (in WeBlog-common) Utility classes
```

### Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Controller | `<Domain>Controller` or `Admin<Domain>Controller` | `ArticleController`, `AdminArticleController` |
| Service Interface | `I<Domain>Service` | `IArticleService` |
| Service Impl | `<Domain>ServiceImpl` | `ArticleServiceImpl` |
| Mapper | `<Domain>Mapper` | `ArticleMapper` |
| Entity | Simple noun | `Article`, `Category`, `User` |
| DTO | `<Domain><Action>DTO` | `ArticlePublishDTO`, `LoginDTO` |
| VO | `<Domain><Context>VO` | `ArticleDetailVO`, `CategorySimpleVO` |

### Imports (ordered)
1. `com.baomidou.mybatisplus.*`
2. `com.blog.*` (wildcard imports acceptable for internal packages)
3. `io.swagger.v3.oas.annotations.*`
4. `jakarta.validation.*`
5. `lombok.*`
6. `org.springframework.*`
7. `org.springframework.security.*`
8. `java.*` / `javax.*`

### Lombok Annotations

| Annotation | Usage |
|------------|-------|
| `@Data` | All entities, DTOs, VOs, result classes |
| `@RequiredArgsConstructor` | All controllers, service impls, configs, filters |
| `@Slf4j` | Controllers, service impls, exception handler, utils |
| `@EqualsAndHashCode(callSuper = false)` | All entities |
| `@Accessors(chain = true)` | All entities |

### Entity Template
```java
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("snake_case_table")
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 字段说明 */
    private String title;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### Controller Conventions
- **All endpoints use `@PostMapping`** — no GET/PUT/DELETE
- Class-level: `@Tag(name = "...")`, `@RestController`, `@RequiredArgsConstructor`, `@RequestMapping("/path")`
- Method-level: `@Operation(summary = "...")`, `@Valid @RequestBody` for DTOs
- Return type: always `Result<T>` or raw `Result`
- Admin write ops: `@PreAuthorize("hasRole('ADMIN')")`
- Admin routes prefixed with `/admin/`; frontend routes are flat

### Service Conventions
- Interfaces extend `IService<Entity>` from MyBatis-Plus
- Impl classes extend `ServiceImpl<Mapper, Entity>`
- Write methods: `@Transactional(rollbackFor = Exception.class)`
- Inject mappers as `private final` fields via `@RequiredArgsConstructor`

### MyBatis-Plus Query Pattern
```java
new LambdaQueryWrapper<Article>()
    .eq(Article::getIsDeleted, 0)
    .like(StringUtils.isNotBlank(keyword), Article::getTitle, keyword)
    .orderByDesc(Article::getCreateTime)
```

### Error Handling
- Throw `BusinessException("中文错误信息")` for business logic errors
- `GlobalExceptionHandler` (`@RestControllerAdvice`) catches:
  - `BusinessException` → `Result.fail(message)`
  - `MethodArgumentNotValidException` → `Result.fail(firstFieldError)`
  - `DuplicateKeyException` → `Result.fail("数据已存在")`
  - `AccessDeniedException` → `Result.fail("权限不足")`

### Response Format
```java
Result.ok()              // success, no data
Result.ok(data)          // success with data
Result.fail(message)     // failure
Result.page(iPage)       // wraps MyBatis-Plus IPage into PageResult
```

---

## Frontend Code Style (Vue 3)

### Component Patterns
- **`<script setup>`** is the standard — Composition API only
- `ref()` for primitives, `reactive()` for form objects
- `defineProps()`, `defineEmits()`, `defineExpose()` for component API
- `onMounted()` / `onBeforeUnmount()` for lifecycle

### File Naming
- **Pages**: kebab-case (`article-list.vue`, `blog-setting.vue`)
- **Shared components**: PascalCase (`CountTo.vue`, `FormDrawer.vue`)
- **Layouts**: PascalCase (`AdminHeader.vue`, `AdminMenu.vue`)

### Import Order
1. Vue core: `import { ref, reactive, onMounted } from 'vue'`
2. Router/Store: `import { useRouter } from 'vue-router'`
3. API: `import { publishArticle } from '@/api/admin/article'`
4. Third-party libs
5. Composables: `import { showMessage } from '@/composables/util'`
6. Components

### API Call Pattern
```javascript
import axios from "@/axios"

export function getArticleDetail(params) {
    return axios.post("/article/detail", params)
}
```
- **All HTTP calls use POST** — no GET/PUT/DELETE
- Response check: `if (res.success == true) { ... }`
- Errors handled by axios interceptor (401 → logout + reload)

### Styling
- **Tailwind CSS** utility classes primary
- `<style scoped>` for component-specific overrides
- `:deep()` for penetrating Element Plus styles
- Element Plus for admin UI; Flowbite + Tailwind for frontend pages

### State Management
- Vuex 4, single flat module (no namespacing)
- Mutations: `SET_USERINFO`, `SET_BLOG_SETTING` (UPPER_SNAKE_CASE)
- Token stored via cookies (`@vueuse/integrations/useCookies`)

---

## General Rules
- Language: Chinese for Javadoc, validation messages, exception messages, Swagger annotations
- Indentation: 4 spaces (Java), 4 spaces (JS/Vue)
- Braces: K&R style (opening brace on same line)
- No emojis in code or comments
- Commit messages in English
