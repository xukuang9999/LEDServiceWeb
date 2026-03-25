# zhglxt-file-manage 文件管理模块

## 功能特性

### 核心功能
- ✅ 文件上传
- ✅ 文件下载
- ✅ 文件移动
- ✅ 文件删除
- ✅ 文件重命名
- ✅ 文件夹创建
- ✅ 文件预览

### 高级功能
- ✅ 水印处理（图片文件）
- ✅ 缩略图生成（图片文件）
- ✅ 权限控制
- ✅ 文件大小限制（100MB）
- ✅ 文件类型限制
- ✅ elFinder集成

## 技术栈
- Spring Boot 3.x
- elFinder 2.1.x
- Thumbnailator（缩略图）
- Java 2D（水印）
- jQuery

## 配置说明

### 文件存储配置
- 根目录：`${user.home}/zhglxt-files`
- 缩略图目录：`.thumbnails/`
- 最大文件大小：100MB
- 允许的文件类型：jpg,jpeg,png,gif,bmp,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar,mp4,mp3

### 水印配置
```yaml
file-manager:
  watermark:
    enabled: true
    text: "ZHGLEXT"
    opacity: 0.3
    font-size: 20
    color: "#FFFFFF"
```

## API接口

### 文件操作
- `POST /file-manager/upload` - 文件上传
- `GET /file-manager/download?path={path}` - 文件下载
- `GET /file-manager/preview?path={path}` - 文件预览
- `DELETE /file-manager/delete?path={path}` - 文件删除
- `POST /file-manager/move` - 文件移动
- `GET /file-manager/list?directory={dir}` - 文件列表

### elFinder接口
- `GET/POST /file-manager/connector` - elFinder连接器

### 权限接口
- `GET /file-manager/check-permission` - 权限检查

## 前端页面
- `/file-manager/test.html` - 测试页面
- `/file-manager/file-manager.html` - 完整文件管理器

## 集成说明

### 1. 依赖配置
已在主pom.xml中添加：
```xml
<dependency>
    <groupId>com.zhglxt</groupId>
    <artifactId>zhglxt-file-manage</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启动配置
由zhglxt-web模块统一启动，无需单独启动

### 3. 使用示例

#### 上传文件
```javascript
// 使用elFinder
$('#elfinder').elfinder('exec', 'upload');

// 使用API
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('directory', 'uploads');

fetch('/file-manager/upload', {
    method: 'POST',
    body: formData
});
```

#### 预览文件
```javascript
window.open('/file-manager/preview?path=' + encodeURIComponent(filePath));
```

## 目录结构
```
zhglxt-file-manage/
├── src/main/java/com/zhglxt/zhglstfilemanage/
│   ├── controller/          # 控制器
│   ├── service/            # 服务层
│   ├── config/             # 配置类
│   ├── dto/                # 数据传输对象
│   └── util/               # 工具类
├── src/main/resources/
│   ├── application.yml     # 配置文件
│   ├── templates/          # HTML模板
│   └── static/             # 静态资源
└── README.md               # 本文档
```

## 测试步骤

1. 启动项目：`mvn spring-boot:run`
2. 访问测试页面：`http://localhost:8080/file-manager/test.html`
3. 访问文件管理器：`http://localhost:8080/file-manager/file-manager.html`

## 注意事项

1. 确保系统有权限创建存储目录
2. 图片水印功能需要系统中安装Java AWT字体
3. 大文件上传可能需要调整nginx配置
4. 生产环境建议配置HTTPS

## 后续优化
- 支持多用户目录隔离
- 增加文件搜索功能
- 集成云存储服务
- 增加文件版本控制
- 支持在线编辑文档