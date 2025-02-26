// 作业状态
export type HomeworkStatus = 'submitted' | 'unsubmitted'

// 作业发布信息
export interface HomeworkAssignment {
  id: number
  title: string
  description?: string
  deadline?: string
  createTime: string
  createdBy: string
  allowSubmit: boolean // 是否允许提交
  fileFormat: string // 文件格式限制
}

// 作业提交记录
export interface HomeworkSubmission {
  id: number
  assignmentId: number
  title: string // 作业标题（从作业发布信息中获取）
  submitTime: string
  status: HomeworkStatus
  file: File | null
  description?: string
  feedback?: string
  submittedBy: string
} 