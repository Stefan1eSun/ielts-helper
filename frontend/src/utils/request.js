// HTTP请求封装

const API_BASE_URL = 'http://localhost:8080';

class Request {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  // 构建请求选项
  buildOptions(options = {}) {
    // 检查是否是FormData
    const isFormData = options.body instanceof FormData;
    
    const config = {
      headers: {
        ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
        ...options.headers
      },
      ...options
    };

    // 添加token到请求头
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  }

  // 处理响应
  async handleResponse(response) {
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.error || `请求失败: ${response.status}`);
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return {};
  }

  // GET请求
  async get(url, options = {}) {
    // 处理查询参数
    let fullUrl = `${this.baseURL}${url}`;
    if (options.params) {
      const params = new URLSearchParams();
      Object.entries(options.params).forEach(([key, value]) => {
        params.append(key, value);
      });
      const paramsString = params.toString();
      if (paramsString) {
        fullUrl += `?${paramsString}`;
      }
    }
    
    const response = await fetch(fullUrl, this.buildOptions({
      ...options,
      method: 'GET'
    }));
    return this.handleResponse(response);
  }

  // POST请求
  async post(url, data = {}, options = {}) {
    const requestOptions = {
      ...options,
      method: 'POST'
    };
    
    // 如果是FormData，直接使用，否则JSON.stringify
    if (data instanceof FormData) {
      requestOptions.body = data;
    } else {
      requestOptions.body = JSON.stringify(data);
    }
    
    const response = await fetch(`${this.baseURL}${url}`, this.buildOptions(requestOptions));
    return this.handleResponse(response);
  }

  // PUT请求
  async put(url, data = {}, options = {}) {
    const requestOptions = {
      ...options,
      method: 'PUT'
    };
    
    // 如果是rawBody选项，直接使用数据，否则JSON.stringify
    if (options.rawBody) {
      requestOptions.body = data;
    } else if (data instanceof FormData) {
      requestOptions.body = data;
    } else {
      requestOptions.body = JSON.stringify(data);
    }
    
    const response = await fetch(`${this.baseURL}${url}`, this.buildOptions(requestOptions));
    return this.handleResponse(response);
  }

  // DELETE请求
  async delete(url, options = {}) {
    const response = await fetch(`${this.baseURL}${url}`, this.buildOptions({
      ...options,
      method: 'DELETE'
    }));
    return this.handleResponse(response);
  }
}

// 导出单例
export default new Request();
