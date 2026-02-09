# Agent Skill Management API 文档

完整的 RESTful API 文档，用于管理 Agent Skills 的生命周期。

## 基础信息

- **Base URL**: `http://localhost:8080/api/agent-skills/manage`
- **Content-Type**: `application/json` (除文件上传外)

---

## 一、技能部署 API

### 1. 上传 ZIP 部署技能

上传压缩包来部署或更新一个技能。

```http
POST /api/agent-skills/manage/upload
Content-Type: multipart/form-data
```

**请求参数：**

| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `file` | File | ✓ | ZIP 格式的技能包 |
| `skillName` | String | ✗ | 自定义技能名（如不提供则从描述文件中提取）|

**示例请求：**

```bash
curl -X POST http://localhost:8080/api/agent-skills/manage/upload \
  -F "file=@demo-calculator-skill.zip" \
  -F "skillName=my-calculator"
```

**成功响应 (200):**

```json
{
  "success": true,
  "skillName": "my-calculator",
  "message": "Skill deployed successfully"
}
```

**失败响应 (400):**

```json
{
  "success": false,
  "message": "Cannot find skill descriptor in uploaded file"
}
```

---

## 二、技能生命周期 API

### 2. 删除技能

完全删除一个技能及其所有文件。

```http
DELETE /api/agent-skills/manage/{skillName}
```

**示例请求：**

```bash
curl -X DELETE http://localhost:8080/api/agent-skills/manage/my-calculator
```

**成功响应 (200):**

```json
{
  "success": true,
  "message": "Skill deleted successfully"
}
```

---

### 3. 重载技能

从磁盘重新加载技能配置（用于手动触发更新）。

```http
POST /api/agent-skills/manage/{skillName}/reload
```

**示例请求：**

```bash
curl -X POST http://localhost:8080/api/agent-skills/manage/my-calculator/reload
```

**成功响应 (200):**

```json
{
  "success": true,
  "skillName": "my-calculator",
  "message": "Skill reloaded successfully"
}
```

---

### 4. 导出技能

将技能打包为 ZIP 文件下载。

```http
GET /api/agent-skills/manage/{skillName}/export
```

**示例请求：**

```bash
curl -O -J http://localhost:8080/api/agent-skills/manage/my-calculator/export
# 将下载 my-calculator.zip 文件
```

**成功响应：**
- Content-Type: `application/octet-stream`
- Content-Disposition: `attachment; filename="my-calculator.zip"`
- Body: ZIP 文件二进制数据

---

## 三、文件管理 API

### 5. 获取文件列表

获取技能目录中的所有文件和子目录。

```http
GET /api/agent-skills/manage/{skillName}/files
```

**示例请求：**

```bash
curl http://localhost:8080/api/agent-skills/manage/my-calculator/files
```

**成功响应 (200):**

```json
{
  "success": true,
  "skillName": "my-calculator",
  "files": [
    {
      "path": "README.md",
      "isDirectory": false,
      "size": 3124,
      "lastModified": 1704067200000
    },
    {
      "path": "examples",
      "isDirectory": true,
      "size": 0,
      "lastModified": 1704067200000
    },
    {
      "path": "examples/example-usage.md",
      "isDirectory": false,
      "size": 545,
      "lastModified": 1704067200000
    },
    {
      "path": "skill.json",
      "isDirectory": false,
      "size": 1273,
      "lastModified": 1704067200000
    }
  ]
}
```

---

### 6. 读取文件内容

获取指定文件的文本内容。

```http
GET /api/agent-skills/manage/{skillName}/files/{filePath}
```

**示例请求：**

```bash
# 读取根目录文件
curl http://localhost:8080/api/agent-skills/manage/my-calculator/files/skill.json

# 读取子目录文件
curl http://localhost:8080/api/agent-skills/manage/my-calculator/files/examples/example-usage.md
```

**成功响应 (200):**

```json
{
  "success": true,
  "skillName": "my-calculator",
  "path": "skill.json",
  "content": "{\n  \"name\": \"demo-calculator-skill\",\n  ...\n}"
}
```

**错误响应：**
- `404`: 技能或文件不存在
- `400`: 非法文件路径（路径遍历攻击防护）
- `500`: 读取失败

---

### 7. 更新文件内容

更新或创建文件。

```http
PUT /api/agent-skills/manage/{skillName}/files/{filePath}
Content-Type: application/json
```

**请求体：**

```json
{
  "content": "文件内容字符串"
}
```

**示例请求：**

```bash
curl -X PUT http://localhost:8080/api/agent-skills/manage/my-calculator/files/README.md \
  -H "Content-Type: application/json" \
  -d '{
    "content": "# Updated README\n\nNew content here..."
  }'
```

**成功响应 (200):**

```json
{
  "success": true,
  "message": "File saved successfully",
  "skillName": "my-calculator",
  "path": "README.md"
}
```

> **提示**: 如果文件不存在会自动创建，父目录也会自动创建。

---

### 8. 删除文件

删除指定的文件或目录。

```http
DELETE /api/agent-skills/manage/{skillName}/files/{filePath}
```

**示例请求：**

```bash
# 删除文件
curl -X DELETE http://localhost:8080/api/agent-skills/manage/my-calculator/files/temp.txt

# 删除目录（递归删除）
curl -X DELETE http://localhost:8080/api/agent-skills/manage/my-calculator/files/old-examples
```

**成功响应 (200):**

```json
{
  "success": true,
  "message": "File deleted successfully",
  "skillName": "my-calculator",
  "path": "temp.txt"
}
```

---

## 四、查询 API（已有）

### 9. 获取所有技能名称

```http
GET /api/agent-skills/names
```

### 10. 获取技能详情

```http
GET /api/agent-skills/{skillName}
```

可选查询参数：

- `revealScripts` (boolean, 默认 `false`)：当设置为 `true` 时，接口会返回技能描述文件中由加载器披露的脚本内容（`disclosedScripts`）。默认不返回以保护敏感信息。

示例（不披露脚本，默认）：

```bash
curl http://localhost:8080/api/agent-skills/my-new-skill
```

示例（披露脚本内容）：

```bash
curl "http://localhost:8080/api/agent-skills/my-new-skill?revealScripts=true"
```

成功响应（当 revealScripts=true 且存在 disclosedScripts）：

```json
{
  "name": "my-new-skill",
  "description": "My custom skill",
  "version": "1.0.0",
  "requiredParameters": {},
  "optionalParameters": {},
  "instructions": "You are a helpful assistant...",
  "disclosedScripts": {
    "scripts/hello.py": {
      "content": "#!/usr/bin/python\nprint('hello')\n",
      "truncated": false,
      "size": 42
    },
    "install/setup.sh": {
      "content": "#!/bin/sh\n# ... long script truncated ...",
      "truncated": true,
      "size": 12345
    }
  }
}
```

说明：`disclosedScripts` 的值是一个以相对路径为键的对象，值为包含 `content`（文本或被截断的文本）、`truncated`（布尔）和 `size`（字节数）字段的对象。加载器会基于配置 `agent.skill.max-skill-md-size-kb` 对脚本内容进行截断，默认不会在 API 中返回这些内容，必须显式请求。


### 11. 获取所有技能信息

```http
GET /api/agent-skills/all
```

### 12. 执行技能

```http
POST /api/agent-skills/execute/{skillName}
Content-Type: application/json

{
  "parameters": {
    "key": "value"
  }
}
```

### 13. 查找技能

根据请求内容查找能够处理的技能。

```http
POST /api/agent-skills/find
Content-Type: application/json

{
  "request": "用户请求文本"
}
```

---

## 五、完整工作流示例

### 场景：创建并部署一个新技能

#### Step 1: 准备技能文件

创建文件夹结构：

```
my-new-skill/
├── skill.json          # 技能配置
└── README.md           # 说明文档
```

**skill.json:**

```json
{
  "name": "my-new-skill",
  "version": "1.0.0",
  "description": "My custom skill",
  "enabled": true,
  "instructions": "You are a helpful assistant..."
}
```

#### Step 2: 打包成 ZIP

```bash
# Windows PowerShell
Compress-Archive -Path my-new-skill/* -DestinationPath my-new-skill.zip

# Linux/Mac
zip -r my-new-skill.zip my-new-skill/
```

#### Step 3: 部署

```bash
curl -X POST http://localhost:8080/api/agent-skills/manage/upload \
  -F "file=@my-new-skill.zip"
```

#### Step 4: 验证部署

```bash
# 查看技能列表
curl http://localhost:8080/api/agent-skills/names

# 获取技能详情
curl http://localhost:8080/api/agent-skills/my-new-skill
```

#### Step 5: 在线编辑（可选）

```bash
# 查看文件列表
curl http://localhost:8080/api/agent-skills/manage/my-new-skill/files

# 读取配置文件
curl http://localhost:8080/api/agent-skills/manage/my-new-skill/files/skill.json

# 修改配置
curl -X PUT http://localhost:8080/api/agent-skills/manage/my-new-skill/files/skill.json \
  -H "Content-Type: application/json" \
  -d '{
    "content": "{\n  \"name\": \"my-new-skill\",\n  \"version\": \"1.1.0\",\n  ...\n}"
  }'

# 热加载会自动触发，无需手动重载！
```

#### Step 6: 导出备份

```bash
curl -O -J http://localhost:8080/api/agent-skills/manage/my-new-skill/export
```

---

## 六、注意事项

### 1. 安全性

- 所有文件路径都经过安全检查，防止路径遍历攻击
- 无法通过 `../` 等方式访问技能目录之外的文件

### 2. 热加载

- 通过 API 更新文件后，**系统会自动触发重载**
- 如需手动控制，可以调用 `/reload` 接口

### 3. ZIP 文件格式

支持的描述文件（按优先级）：
1. `skill.json` - JSON 格式
2. `skill.yaml` / `skill.yml` - YAML 格式
3. `SKILL.md` - Markdown 格式 (agentskills.io 规范)

ZIP 结构支持两种方式：

```
# 方式 1: 直接包含技能文件
skill.zip
├── skill.json
└── README.md

# 方式 2: 包含父文件夹（更常见）
skill.zip
└── my-skill/
    ├── skill.json
    └── README.md
```

### 4. 错误处理

所有 API 返回统一的响应格式：

```json
{
  "success": boolean,
  "message": "操作结果描述",
  // 其他字段...
}
```

HTTP 状态码：
- `200 OK` - 操作成功
- `400 Bad Request` - 请求参数错误
- `404 Not Found` - 技能或文件不存在
- `500 Internal Server Error` - 服务器内部错误

---

## 七、示例代码

### Python 示例

```python
import requests

base_url = "http://localhost:8080/api/agent-skills/manage"

# 部署技能
with open("my-skill.zip", "rb") as f:
    files = {"file": ("my-skill.zip", f, "application/zip")}
    response = requests.post(f"{base_url}/upload", files=files)
    print(response.json())

# 读取文件
response = requests.get(f"{base_url}/my-skill/files/skill.json")
data = response.json()
print(data["content"])

# 更新文件
response = requests.put(
    f"{base_url}/my-skill/files/README.md",
    json={"content": "# New Content"}
)
print(response.json())
```

### JavaScript/Node.js 示例

```javascript
const FormData = require('form-data');
const fs = require('fs');
const axios = require('axios');

const baseUrl = 'http://localhost:8080/api/agent-skills/manage';

// 部署技能
async function deploySkill() {
  const form = new FormData();
  form.append('file', fs.createReadStream('my-skill.zip'));
  
  const response = await axios.post(`${baseUrl}/upload`, form, {
    headers: form.getHeaders()
  });
  console.log(response.data);
}

// 读取并更新文件
async function updateFile() {
  // 读取
  const { data } = await axios.get(
    `${baseUrl}/my-skill/files/skill.json`
  );
  console.log(data.content);
  
  // 更新
  await axios.put(
    `${baseUrl}/my-skill/files/skill.json`,
    { content: '{"name": "my-skill", "version": "2.0.0"}' }
  );
}
```

---

## 八、相关资源

- **示例技能包**: `demo-calculator-skill.zip`
- **示例技能源码**: `demo-skill-example/` 目录
- **技能格式规范**: [agentskills.io](https://agentskills.io)

---

如有问题，请检查：
1. 服务是否已启动
2. ZIP 文件格式是否正确
3. 技能描述文件是否存在且格式正确
