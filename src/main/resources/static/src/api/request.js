import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// request.js 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data;
        
        // 修改为 != 以兼容数字和字符串类型
        if (res.code != 200) {
            // 打印出导致问题的结构，方便调试
            console.error('拦截器捕获到非200响应:', res); 
            ElMessage.error(res.message || '系统发生错误');
            return Promise.reject(new Error(res.message || 'Error'));
        }
        
        return res.data; 
    },
    error => {
        console.error('请求发生异常:', error);
        ElMessage.error('网络连接错误');
        return Promise.reject(error);
    }
)

export default request