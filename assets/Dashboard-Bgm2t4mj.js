import{d as e,r as a,o as t,a as l,c as r,b as o,w as i,e as n,f as s,E as d,t as u,h as c,v as p,k as f,l as m,m as v,x as g,u as y,g as h,q as w}from"./index-Crrk48_U.js";const b={class:"dashboard-container"},_={class:"header-content"},k={class:"user-info"},x={key:1,class:"loading-container"},C={key:2},S={class:"dialog-footer"},I=w(e({__name:"Dashboard",setup(e){const w=y(),I=a(localStorage.getItem("username")||""),$=a(!1),T=a(null),B=a(),F=a([]),U=a([]),V=a("checking"),A=a(!0),D=async()=>{var e,a;try{const e=await s.get("/api/submissions/user");U.value=e.data}catch(t){d.error((null==(a=null==(e=t.response)?void 0:e.data)?void 0:a.error)||"获取提交记录失败")}},O=async()=>{A.value=!0;try{if(!(await(async()=>{try{return await s.get("/api/health",{timeout:5e3}),V.value="online",u(!1),!0}catch(e){return V.value="offline",u(!0),!1}})())){const e=localStorage.getItem("cachedAssignments");return e&&(F.value=JSON.parse(e),d.warning("显示的是缓存数据，可能不是最新的")),void(A.value=!1)}const e=await s.get("/api/assignments");F.value=e.data,localStorage.setItem("cachedAssignments",JSON.stringify(e.data))}catch(e){if(e.response)d.error("获取作业列表失败");else{V.value="offline",u(!0);const e=localStorage.getItem("cachedAssignments");e&&(F.value=JSON.parse(e),d.warning("显示的是缓存数据，可能不是最新的"))}}finally{A.value=!1}};t((()=>{O(),D()}));const R=l({file:null,description:""}),L=()=>{g.confirm("确定要退出登录吗？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{localStorage.removeItem("token"),localStorage.removeItem("isAdmin"),localStorage.removeItem("username"),w.push("/")}))},j=e=>{T.value=e,R.file=null,R.description="",$.value=!0},q=e=>{if(e.raw){if("application/pdf"!==e.raw.type)return d.error("只能上传PDF文件！"),!1;R.file=e.raw}},E=async()=>{B.value&&(T.value?R.file?await B.value.validate((async e=>{var a;if(e)try{if(!T.value||!R.file)return;const e=T.value.id,a=(T.value.title,R.file);if("application/pdf"!==a.type)return void d.error("只能上传PDF文件");if(a.size>20971520)return void d.error("文件大小不能超过20MB");const t=new FormData;t.append("file",a),R.description&&t.append("description",R.description);const l=await s.post(`/api/submissions/assignment/${e}`,t,{headers:{"Content-Type":"multipart/form-data"},timeout:3e4});U.value.unshift(l.data),d.success("作业提交成功！"),$.value=!1,O(),D()}catch(t){if(t.response){const e=(null==(a=t.response.data)?void 0:a.error)||"提交作业失败";d.error(e)}else t.request?d.error("服务器未响应，请检查网络连接"):d.error(`请求错误: ${t.message}`)}else d.error("请填写完整的表单")})):d.error("请选择要上传的文件"):d.error("当前作业不存在"))};return(e,a)=>{var t;const l=n("el-button"),u=n("el-header"),y=n("el-alert"),w=n("el-skeleton"),D=n("el-table-column"),O=n("el-table"),J=n("el-card"),N=n("el-col"),P=n("el-tag"),z=n("el-row"),M=n("el-main"),G=n("el-container"),H=n("el-upload"),K=n("el-form-item"),Q=n("el-input"),W=n("el-form"),X=n("el-dialog");return h(),r("div",b,[o(G,null,{default:i((()=>[o(u,null,{default:i((()=>[c("div",_,[a[4]||(a[4]=c("h2",null,"作业提交系统",-1)),c("div",k,[c("span",null,p(I.value),1),o(l,{type:"text",onClick:L},{default:i((()=>a[3]||(a[3]=[f("退出登录")]))),_:1})])])])),_:1}),o(M,null,{default:i((()=>["offline"===V.value?(h(),m(y,{key:0,title:"服务器连接失败，数据可能不是最新的",type:"warning",closable:!1,"show-icon":"",style:{"margin-bottom":"20px"}})):v("",!0),A.value?(h(),r("div",x,[o(w,{rows:3,animated:""})])):(h(),r("div",C,[o(z,{gutter:20},{default:i((()=>[o(N,{span:24,style:{"margin-bottom":"20px"}},{default:i((()=>[o(J,null,{header:i((()=>a[5]||(a[5]=[c("div",{class:"card-header"},[c("span",null,"可提交的作业")],-1)]))),default:i((()=>[o(O,{data:F.value,style:{width:"100%"}},{default:i((()=>[o(D,{prop:"title",label:"作业标题"}),o(D,{prop:"description",label:"作业描述"}),o(D,{prop:"deadline",label:"截止时间"}),o(D,{label:"操作"},{default:i((({row:e})=>[o(l,{type:"primary",onClick:a=>j(e)},{default:i((()=>a[6]||(a[6]=[f("提交作业")]))),_:2},1032,["onClick"])])),_:1})])),_:1},8,["data"])])),_:1})])),_:1}),o(N,{span:24},{default:i((()=>[o(J,null,{header:i((()=>a[7]||(a[7]=[c("div",{class:"card-header"},[c("span",null,"提交记录")],-1)]))),default:i((()=>[o(O,{data:U.value,style:{width:"100%"}},{default:i((()=>[o(D,{prop:"title",label:"作业标题"}),o(D,{prop:"submitTime",label:"提交时间"}),o(D,{prop:"originalFilename",label:"文件名"},{default:i((({row:e})=>[f(p(e.originalFilename||"未知文件名"),1)])),_:1}),o(D,{prop:"status",label:"状态"},{default:i((({row:e})=>[o(P,{type:"submitted"===e.status?"success":"info"},{default:i((()=>[f(p("submitted"===e.status?"已提交":"未提交"),1)])),_:2},1032,["type"])])),_:1}),o(D,{label:"操作"},{default:i((({row:e})=>[o(l,{type:"primary",onClick:a=>(async e=>{var a,t;try{let a=(await s.get(`/api/submissions/${e.id}`)).data.originalFilename||`作业-${e.id}.pdf`;a&&""!==a.trim()||(a=`作业-${e.id}.pdf`);const t=d.info({message:"正在下载文件...",duration:0}),l=await fetch(`/api/submissions/${e.id}/file`,{headers:{Authorization:`Bearer ${localStorage.getItem("token")}`}});if(!l.ok)throw new Error(`下载失败: ${l.status} ${l.statusText}`);const r=await l.blob();t.close();const o=window.URL.createObjectURL(r),i=document.createElement("a");i.href=o,i.download=a,document.body.appendChild(i),i.click(),window.URL.revokeObjectURL(o),document.body.removeChild(i),d.success("文件下载成功")}catch(l){l.response,d.error((null==(t=null==(a=l.response)?void 0:a.data)?void 0:t.error)||l.message||"下载文件失败")}})(e)},{default:i((()=>a[8]||(a[8]=[f("查看")]))),_:2},1032,["onClick"]),o(l,{type:"warning",onClick:a=>(e=>{const a=F.value.find((a=>a.id===e.assignmentId));a?g.confirm("重新提交将会删除您之前提交的文件，确定要继续吗？","确认重新提交",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{j(a)})).catch((()=>{d.info("已取消重新提交")})):d.warning("该作业已不允许提交")})(e)},{default:i((()=>a[9]||(a[9]=[f("重新提交")]))),_:2},1032,["onClick"]),o(l,{type:"danger",onClick:a=>(async e=>{g.confirm("确定要删除这个作业吗？","警告",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((async()=>{var a,t;try{await s.delete(`/api/submissions/${e.id}`),U.value=U.value.filter((a=>a.id!==e.id)),d.success("删除成功")}catch(l){d.error((null==(t=null==(a=l.response)?void 0:a.data)?void 0:t.error)||"删除作业失败")}}))})(e)},{default:i((()=>a[10]||(a[10]=[f("删除")]))),_:2},1032,["onClick"])])),_:1})])),_:1},8,["data"])])),_:1})])),_:1})])),_:1})]))])),_:1})])),_:1}),o(X,{modelValue:$.value,"onUpdate:modelValue":a[2]||(a[2]=e=>$.value=e),title:null==(t=T.value)?void 0:t.title,width:"50%"},{footer:i((()=>[c("span",S,[o(l,{onClick:a[1]||(a[1]=e=>$.value=!1)},{default:i((()=>a[13]||(a[13]=[f("取消")]))),_:1}),o(l,{type:"primary",onClick:E},{default:i((()=>a[14]||(a[14]=[f(" 提交 ")]))),_:1})])])),default:i((()=>[o(W,{ref_key:"submitFormRef",ref:B,model:R,"label-width":"120px"},{default:i((()=>[o(K,{label:"作业文件",prop:"file",rules:[{required:!0,message:"请上传作业文件"}]},{default:i((()=>[o(H,{class:"upload-container","auto-upload":!1,limit:1,accept:".pdf","on-change":q},{trigger:i((()=>[o(l,{type:"primary",class:"upload-button"},{default:i((()=>a[11]||(a[11]=[f("选择文件")]))),_:1})])),tip:i((()=>a[12]||(a[12]=[c("div",{class:"el-upload__tip"}," 只能上传 PDF 文件 ",-1)]))),_:1})])),_:1}),o(K,{label:"备注",prop:"description"},{default:i((()=>[o(Q,{modelValue:R.description,"onUpdate:modelValue":a[0]||(a[0]=e=>R.description=e),type:"textarea",placeholder:"请输入备注信息（可选）"},null,8,["modelValue"])])),_:1})])),_:1},8,["model"])])),_:1},8,["modelValue","title"])])}}}),[["__scopeId","data-v-6a8a7270"]]);export{I as default};
